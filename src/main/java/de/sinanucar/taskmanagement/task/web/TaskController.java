package de.sinanucar.taskmanagement.task.web;

import de.sinanucar.taskmanagement.task.application.TaskService;
import de.sinanucar.taskmanagement.task.domain.Priority;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @InitBinder("form")
    void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("name", "priority");
    }

    @ModelAttribute("priorityLabels")
    Map<Priority, String> priorityLabels() {
        return Map.of(
                Priority.LOW, "Niedrig", Priority.NORMAL, "Normal", Priority.URGENT, "Dringend");
    }

    @GetMapping("/")
    String home() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    String list(Model model) {
        CreateTaskForm form = new CreateTaskForm();
        form.setPriority(Priority.NORMAL);
        model.addAttribute("form", form);
        model.addAttribute("tasks", service.listTasks());
        return "tasks/list";
    }

    @PostMapping("/tasks")
    Object create(
            @Valid @ModelAttribute("form") CreateTaskForm form,
            BindingResult errors,
            Model model,
            HttpServletResponse response) {
        if (errors.hasErrors()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("tasks", service.listTasks());
            return "tasks/list";
        }
        service.createTask(form.getName(), form.getPriority());
        return redirectToTasks();
    }

    @InitBinder("renameForm")
    void configureRenameBinding(WebDataBinder binder) {
        binder.setAllowedFields("name");
    }

    @GetMapping("/tasks/{id}/edit")
    String edit(@PathVariable Long id, Model model) {
        var task = service.getTask(id);
        RenameTaskForm form = new RenameTaskForm();
        form.setName(task.name());
        model.addAttribute("renameForm", form);
        model.addAttribute("taskId", id);
        return "tasks/edit";
    }

    @PostMapping("/tasks/{id}/name/preview")
    String previewName(
            @PathVariable Long id,
            @Valid @ModelAttribute("renameForm") RenameTaskForm form,
            BindingResult errors,
            Model model,
            HttpServletResponse response) {
        var task = service.getTask(id);
        model.addAttribute("taskId", id);
        if (errors.hasErrors()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "tasks/edit";
        }
        model.addAttribute("task", task);
        return "tasks/confirm-name";
    }

    @GetMapping("/tasks/{id}/delete/confirm")
    String confirmDelete(@PathVariable Long id, Model model) {
        model.addAttribute("task", service.getTask(id));
        return "tasks/confirm-delete";
    }

    @PostMapping("/tasks/{id}/name")
    Object rename(
            @PathVariable Long id,
            @Valid @ModelAttribute("renameForm") RenameTaskForm form,
            BindingResult errors,
            Model model,
            HttpServletResponse response) {
        service.getTask(id);
        if (errors.hasErrors()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("taskId", id);
            return "tasks/edit";
        }
        service.renameTask(id, form.getName());
        return redirectToTasks();
    }

    @PostMapping("/tasks/{id}/done")
    RedirectView setDone(
            @PathVariable Long id,
            @RequestParam String done,
            @RequestParam(defaultValue = "false") boolean undo,
            RedirectAttributes redirect) {
        if (!"true".equals(done) && !"false".equals(done)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST);
        }
        var previous = service.getTask(id);
        service.setTaskDone(id, Boolean.parseBoolean(done));
        if (!undo) {
            redirect.addFlashAttribute("undoTask", previous);
        }
        return redirectToTasks();
    }

    @PostMapping("/tasks/{id}/delete")
    RedirectView delete(@PathVariable Long id) {
        service.deleteTask(id);
        return redirectToTasks();
    }

    private RedirectView redirectToTasks() {
        RedirectView redirect = new RedirectView("/tasks", true);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);
        return redirect;
    }
}
