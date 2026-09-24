package de.sinanucar.taskmanagement.task.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.sinanucar.taskmanagement.task.domain.*;
import de.sinanucar.taskmanagement.task.persistence.TaskRepository;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskServiceTest {
    private final TaskRepository repository = mock(TaskRepository.class);
    private final Instant now = Instant.parse("2026-09-24T12:30:00Z");
    private final TaskService service =
            new TaskService(repository, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void createsNormalizedOpenTaskWithServerTime() {
        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        TaskData result = service.createTask("  Planen  ", Priority.URGENT);
        assertThat(result.name()).isEqualTo("Planen");
        assertThat(result.done()).isFalse();
        assertThat(result.created()).isEqualTo(now);
        assertThat(result.priority()).isEqualTo(Priority.URGENT);
    }

    @Test
    void rejectsInvalidInputBeforeSaving() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.createTask(" ", Priority.NORMAL));
        assertThatNullPointerException().isThrownBy(() -> service.createTask("Task", null));
        verifyNoInteractions(repository);
    }

    @Test
    void returnsImmutableSnapshotsInRepositoryOrder() {
        Task entity = new Task("Before", Priority.LOW, now);
        when(repository.findAllByOrderByCreatedDescIdDesc()).thenReturn(List.of(entity));
        List<TaskData> result = service.listTasks();
        entity.rename("After");
        assertThat(result).extracting(TaskData::name).containsExactly("Before");
        assertThatThrownBy(() -> result.clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void renamePreservesStatusPriorityAndTime() {
        Task task = new Task("Before", Priority.URGENT, now);
        task.setDone(true);
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(task));
        service.renameTask(7L, " After ");
        assertThat(task.getName()).isEqualTo("After");
        assertThat(task.isDone()).isTrue();
        assertThat(task.getPriority()).isEqualTo(Priority.URGENT);
        assertThat(task.getCreated()).isEqualTo(now);
        assertThatIllegalArgumentException().isThrownBy(() -> service.renameTask(7L, " "));
        assertThat(task.getName()).isEqualTo("After");
    }

    @Test
    void setsStatusIdempotentlyAndPreservesOtherFields() {
        Task task = new Task("Task", Priority.LOW, now);
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(task));
        service.setTaskDone(7L, true);
        service.setTaskDone(7L, true);
        assertThat(task.isDone()).isTrue();
        service.setTaskDone(7L, false);
        assertThat(task.isDone()).isFalse();
        assertThat(task.getName()).isEqualTo("Task");
        assertThat(task.getCreated()).isEqualTo(now);
        assertThat(task.getPriority()).isEqualTo(Priority.LOW);
    }

    @Test
    void rejectsMissingTaskForEveryOperation() {
        when(repository.findById(7L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> service.getTask(7L)).isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> service.renameTask(7L, "Name"))
                .isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> service.setTaskDone(7L, true))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deletesExistingTaskAndRejectsMissingTask() {
        Task task = new Task("Task", Priority.NORMAL, now);
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(task));
        service.deleteTask(7L);
        verify(repository).delete(task);
        when(repository.findById(7L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> service.deleteTask(7L)).isInstanceOf(TaskNotFoundException.class);
        verify(repository, times(1)).delete(any(Task.class));
    }
}
