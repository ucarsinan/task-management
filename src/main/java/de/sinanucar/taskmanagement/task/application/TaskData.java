package de.sinanucar.taskmanagement.task.application;

import de.sinanucar.taskmanagement.task.domain.Priority;
import de.sinanucar.taskmanagement.task.domain.Task;
import java.time.Instant;

public record TaskData(Long id, String name, boolean done, Instant created, Priority priority) {
    static TaskData from(Task task) {
        return new TaskData(
                task.getId(), task.getName(), task.isDone(), task.getCreated(), task.getPriority());
    }
}
