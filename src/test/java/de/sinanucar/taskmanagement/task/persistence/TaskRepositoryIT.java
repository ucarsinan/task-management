package de.sinanucar.taskmanagement.task.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import de.sinanucar.taskmanagement.task.domain.Priority;
import de.sinanucar.taskmanagement.task.domain.Task;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class TaskRepositoryIT {
    private static final Instant CREATED = Instant.parse("2026-09-24T12:00:00.123456Z");

    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17.9-alpine");

    @Autowired private TaskRepository repository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearTasks() {
        repository.deleteAll();
    }

    @Test
    void appliesVersionedMigrationOnRealPostgres() {
        assertThat(jdbc.queryForObject("SELECT version()", String.class))
                .startsWith("PostgreSQL 17.9");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM flyway_schema_history WHERE version = '1' AND success",
                                Integer.class))
                .isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void storesEnumNamesAndRoundTripsTasks(Priority priority) {
        Instant preciseTime = CREATED.plusNanos(789);
        Task saved = repository.saveAndFlush(new Task("Persisted", priority, preciseTime));
        Task reloaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getName()).isEqualTo("Persisted");
        assertThat(reloaded.isDone()).isFalse();
        assertThat(reloaded.getCreated()).isCloseTo(preciseTime, within(1, ChronoUnit.MICROS));
        assertThat(reloaded.getPriority()).isEqualTo(priority);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT priority FROM tasks WHERE id = ?",
                                String.class,
                                saved.getId()))
                .isEqualTo(priority.name());
    }

    @Test
    void sortsByNewestCreationTimeThenDescendingId() {
        Task older =
                repository.saveAndFlush(
                        new Task("Older", Priority.URGENT, CREATED.minusSeconds(1)));
        Task first = repository.saveAndFlush(new Task("First", Priority.NORMAL, CREATED));
        Task second = repository.saveAndFlush(new Task("Second", Priority.LOW, CREATED));
        assertThat(repository.findAllByOrderByCreatedDescIdDesc())
                .extracting(Task::getId)
                .containsExactly(second.getId(), first.getId(), older.getId());
    }

    @Test
    void commitsChangesWithoutChangingCreationTimeOrPriority() {
        Long id = repository.saveAndFlush(new Task("Before", Priority.URGENT, CREATED)).getId();
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(
                        status -> {
                            Task task = repository.findById(id).orElseThrow();
                            task.rename("After");
                            task.setDone(true);
                        });
        Task reloaded = repository.findById(id).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("After");
        assertThat(reloaded.isDone()).isTrue();
        assertThat(reloaded.getCreated()).isEqualTo(CREATED);
        assertThat(reloaded.getPriority()).isEqualTo(Priority.URGENT);
    }

    @Test
    void rollsBackUncommittedChanges() {
        Long id = repository.saveAndFlush(new Task("Before", Priority.NORMAL, CREATED)).getId();
        assertThatThrownBy(
                        () ->
                                new TransactionTemplate(transactionManager)
                                        .executeWithoutResult(
                                                status -> {
                                                    Task task =
                                                            repository.findById(id).orElseThrow();
                                                    task.rename("Must roll back");
                                                    repository.flush();
                                                    throw new IllegalStateException(
                                                            "Abort transaction");
                                                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(repository.findById(id).orElseThrow().getName()).isEqualTo("Before");
    }

    @Test
    void deletesPermanently() {
        Long id = repository.saveAndFlush(new Task("Delete", Priority.NORMAL, CREATED)).getId();
        repository.deleteById(id);
        assertThat(repository.findById(id)).isEmpty();
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM tasks WHERE id = ?", Integer.class, id))
                .isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void databaseRejectsBlankNames(String name) {
        assertThatThrownBy(() -> insertRaw(name, "NORMAL"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsInvalidPriorityAndNullRequiredValues() {
        assertThatThrownBy(() -> insertRaw("Task", "OTHER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertRaw("Task", null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertRaw(null, "NORMAL"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertRaw("a".repeat(201), "NORMAL"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "INSERT INTO tasks (name, done, created, priority) VALUES ('Task', NULL, ?, 'NORMAL')",
                                        Timestamp.from(CREATED)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "INSERT INTO tasks (name, done, created, priority) VALUES ('Task', FALSE, NULL, 'NORMAL')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertRaw(String name, String priority) {
        jdbc.update(
                "INSERT INTO tasks (name, done, created, priority) VALUES (?, FALSE, ?, ?)",
                name,
                Timestamp.from(CREATED),
                priority);
    }
}
