package de.sinanucar.taskmanagement.task.web;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.sinanucar.taskmanagement.config.SecurityConfiguration;
import de.sinanucar.taskmanagement.task.application.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfiguration.class)
class TaskErrorHandlerTest {
    @Autowired MockMvc mvc;
    @MockitoBean TaskService service;

    @Test
    void databaseErrorsReturnNeutralHtmlWithoutTechnicalDetails() throws Exception {
        when(service.listTasks())
                .thenThrow(new DataAccessResourceFailureException("internal-db-detail"));
        mvc.perform(get("/tasks"))
                .andExpect(status().isInternalServerError())
                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "Die Anfrage konnte nicht abgeschlossen werden.")))
                .andExpect(content().string(not(containsString("internal-db-detail"))));
    }
}
