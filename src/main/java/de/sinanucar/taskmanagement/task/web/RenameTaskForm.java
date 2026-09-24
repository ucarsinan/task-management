package de.sinanucar.taskmanagement.task.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RenameTaskForm {
    @NotBlank(message = "Bitte gib einen Namen ein.")
    @Size(max = 200, message = "Der Name darf höchstens 200 Zeichen lang sein.")
    @Pattern(regexp = "[^\\x00]*", message = "Der Name enthält ein ungültiges Zeichen.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.strip();
    }
}
