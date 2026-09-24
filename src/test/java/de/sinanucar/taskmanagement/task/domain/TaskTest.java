package de.sinanucar.taskmanagement.task.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TaskTest {
    private static final Instant CREATED = Instant.parse("2026-09-24T12:00:00.123456Z");

    @Test
    void createsAnOpenTaskWithNormalizedName() {
        Task task = new Task("  Aufgabe  ", Priority.NORMAL, CREATED);
        assertThat(task.getName()).isEqualTo("Aufgabe");
        assertThat(task.isDone()).isFalse();
        assertThat(task.getCreated()).isEqualTo(CREATED);
        assertThat(task.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(task.getId()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", "\u2003", "invalid\u0000name"})
    void rejectsMissingOrBlankNames(String name) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Task(name, Priority.LOW, CREATED));
    }

    @Test
    void enforcesLengthAfterStrippingAndCountsJavaCodeUnits() {
        assertThat(new Task(" " + "a".repeat(200) + " ", Priority.LOW, CREATED).getName())
                .hasSize(200);
        assertThat(new Task("😀".repeat(100), Priority.LOW, CREATED).getName()).hasSize(200);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Task("a".repeat(201), Priority.LOW, CREATED));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Task("😀".repeat(101), Priority.LOW, CREATED));
    }

    @Test
    void rejectsMissingPriorityOrCreationTime() {
        assertThatNullPointerException().isThrownBy(() -> new Task("Task", null, CREATED));
        assertThatNullPointerException().isThrownBy(() -> new Task("Task", Priority.LOW, null));
    }

    @Test
    void renamingAndSettingDonePreserveOtherFields() {
        Task task = new Task("Before", Priority.URGENT, CREATED);
        task.rename(" After ");
        task.setDone(true);
        task.setDone(true);
        assertThat(task.getName()).isEqualTo("After");
        assertThat(task.isDone()).isTrue();
        assertThat(task.getCreated()).isEqualTo(CREATED);
        assertThat(task.getPriority()).isEqualTo(Priority.URGENT);
        task.setDone(false);
        assertThat(task.isDone()).isFalse();
    }

    @Test
    void invalidRenameDoesNotChangeState() {
        Task task = new Task("Before", Priority.NORMAL, CREATED);
        assertThatIllegalArgumentException().isThrownBy(() -> task.rename(" "));
        assertThat(task.getName()).isEqualTo("Before");
    }
}
