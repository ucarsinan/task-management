package de.sinanucar.taskmanagement.task.persistence;

import de.sinanucar.taskmanagement.task.domain.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByCreatedDescIdDesc();
}
