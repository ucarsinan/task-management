package de.sinanucar.taskmanagement.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean done;

    @Column(nullable = false, updatable = false)
    private Instant created;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 6)
    private Priority priority;

    protected Task() {
        // Required by JPA; application code uses the validated constructor.
    }

    public Task(String name, Priority priority, Instant created) {
        this.name = validatedName(name);
        this.priority = Objects.requireNonNull(priority, "Priority is required");
        this.created = Objects.requireNonNull(created, "Creation time is required");
        this.done = false;
    }

    public void rename(String name) {
        this.name = validatedName(name);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    private static String validatedName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required");
        }
        String normalized = name.strip();
        if (normalized.isEmpty() || normalized.length() > 200 || normalized.indexOf(0) >= 0) {
            throw new IllegalArgumentException("Name must contain between 1 and 200 characters");
        }
        return normalized;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDone() {
        return done;
    }

    public Instant getCreated() {
        return created;
    }

    public Priority getPriority() {
        return priority;
    }
}
