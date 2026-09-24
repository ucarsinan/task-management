package de.sinanucar.taskmanagement.task.web;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class TaskErrorHandler {
    @ExceptionHandler(DataAccessException.class)
    ModelAndView databaseError() {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return view;
    }

    @ExceptionHandler(de.sinanucar.taskmanagement.task.application.TaskNotFoundException.class)
    ModelAndView notFound() {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(HttpStatus.NOT_FOUND);
        view.addObject("errorTitle", "Diese Aufgabe wurde nicht gefunden.");
        return view;
    }

    @ExceptionHandler({
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        org.springframework.web.server.ResponseStatusException.class
    })
    ModelAndView invalidRequest(Exception exception) {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(
                exception
                                instanceof
                                org.springframework.web.server.ResponseStatusException
                                                statusException
                        ? statusException.getStatusCode()
                        : HttpStatus.BAD_REQUEST);
        view.addObject("errorTitle", "Bitte prüfe deine Eingaben.");
        return view;
    }
}
