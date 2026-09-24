package de.sinanucar.taskmanagement.task.application;

import de.sinanucar.taskmanagement.task.domain.*;
import de.sinanucar.taskmanagement.task.persistence.TaskRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository repository;
    private final Clock clock;

    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<TaskData> listTasks() {
        return repository.findAllByOrderByCreatedDescIdDesc().stream().map(TaskData::from).toList();
    }

    @Transactional
    public TaskData createTask(String name, Priority priority) {
        return TaskData.from(repository.save(new Task(name, priority, clock.instant())));
    }

    public TaskData getTask(Long id) {
        return TaskData.from(findTask(id));
    }

    @Transactional
    public void renameTask(Long id, String name) {
        findTask(id).rename(name);
    }

    @Transactional
    public void setTaskDone(Long id, boolean done) {
        findTask(id).setDone(done);
    }

    @Transactional
    public void deleteTask(Long id) {
        repository.delete(findTask(id));
    }

    private Task findTask(Long id) {
        return repository.findById(id).orElseThrow(TaskNotFoundException::new);
    }
}
