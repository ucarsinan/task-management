package de.sinanucar.taskmanagement.task.web;

import de.sinanucar.taskmanagement.task.domain.Priority;
import jakarta.validation.constraints.*;

public class CreateTaskForm {
    @NotBlank(message = "Bitte gib einen Namen ein.")
    @Size(max = 200, message = "Der Name darf höchstens 200 Zeichen lang sein.")
    @Pattern(regexp = "[^\\x00]*", message = "Der Name enthält ein ungültiges Zeichen.")
    private String name;

    @NotNull(message = "Bitte wähle eine gültige Priorität.")
    private Priority priority;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.strip();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
