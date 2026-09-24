package de.sinanucar.taskmanagement.task.web;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.sinanucar.taskmanagement.task.domain.*;
import de.sinanucar.taskmanagement.task.persistence.TaskRepository;
import java.time.Instant;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskWebIT {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17.9-alpine");

    @Autowired MockMvc mvc;
    @Autowired TaskRepository repository;

    @BeforeEach
    void clearTasks() {
        repository.deleteAll();
    }

    @Test
    void rendersEmptyListAndDefaultPriorityWithoutLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
        mvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Noch keine Aufgaben")))
                .andExpect(
                        result -> {
                            var page = Jsoup.parse(result.getResponse().getContentAsString());
                            assertThat(page.selectFirst("#priority option[selected]").val())
                                    .isEqualTo("NORMAL");
                            assertThat(page.select("form input[name=_csrf]")).hasSize(1);
                        });
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());
    }

    @Test
    void createsPersistsAndIgnoresServerOwnedFields() throws Exception {
        Instant before = Instant.now();
        mvc.perform(
                        post("/tasks")
                                .with(csrf())
                                .param("name", "  Neue Aufgabe  ")
                                .param("priority", "URGENT")
                                .param("done", "true")
                                .param("created", "2000-01-01T00:00:00Z")
                                .param("id", "99999"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/tasks"));
        Task task = repository.findAll().get(0);
        assertThat(task.getName()).isEqualTo("Neue Aufgabe");
        assertThat(task.getPriority()).isEqualTo(Priority.URGENT);
        assertThat(task.isDone()).isFalse();
        assertThat(task.getId()).isNotEqualTo(99999L);
        assertThat(task.getCreated()).isAfterOrEqualTo(before.minusSeconds(1));
        mvc.perform(get("/tasks")).andExpect(content().string(containsString("Neue Aufgabe")));
        assertThat(repository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n", "\u2003", "invalid\u0000name"})
    void rejectsInvalidNamesWithoutSaving(String name) throws Exception {
        mvc.perform(post("/tasks").with(csrf()).param("name", name).param("priority", "NORMAL"))
                .andExpect(status().isBadRequest())
                .andExpect(model().attributeHasFieldErrors("form", "name"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void rejectsOversizedNameAndPreservesInputAndList() throws Exception {
        repository.save(new Task("Vorhanden", Priority.NORMAL, Instant.now()));
        String name = "x".repeat(201);
        mvc.perform(post("/tasks").with(csrf()).param("name", name).param("priority", "LOW"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result ->
                                assertThat(
                                                Jsoup.parse(
                                                                result.getResponse()
                                                                        .getContentAsString())
                                                        .selectFirst("#name")
                                                        .val())
                                        .isEqualTo(name))
                .andExpect(content().string(containsString("Vorhanden")));
        assertThat(repository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"OTHER", "", "low"})
    void rejectsInvalidPriorities(String priority) throws Exception {
        mvc.perform(post("/tasks").with(csrf()).param("name", "Task").param("priority", priority))
                .andExpect(status().isBadRequest())
                .andExpect(model().attributeHasFieldErrors("form", "priority"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void rejectsMissingPriority() throws Exception {
        mvc.perform(post("/tasks").with(csrf()).param("name", "Task"))
                .andExpect(status().isBadRequest())
                .andExpect(model().attributeHasFieldErrors("form", "priority"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void rejectsMissingAndInvalidCsrf() throws Exception {
        mvc.perform(post("/tasks").param("name", "Task").param("priority", "NORMAL"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/tasks")
                                .with(csrf().useInvalidToken())
                                .param("name", "Task")
                                .param("priority", "NORMAL"))
                .andExpect(status().isForbidden());
        assertThat(repository.count()).isZero();
    }

    @Test
    void escapesTaskNamesAndRendersUtcTime() throws Exception {
        repository.save(
                new Task(
                        "<script>alert(1)</script>",
                        Priority.LOW,
                        Instant.parse("2026-09-24T12:30:00Z")));
        mvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(containsString("24.09.2026 12:30")))
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        containsString("form-action 'self'")));
    }

    @Test
    void rendersEditFormAndPersistsOnlyName() throws Exception {
        Instant created = Instant.parse("2026-09-24T12:30:00Z");
        Task task = new Task("Original", Priority.URGENT, created);
        task.setDone(true);
        Long id = repository.saveAndFlush(task).getId();
        mvc.perform(get("/tasks/{id}/edit", id))
                .andExpect(status().isOk())
                .andExpect(
                        result ->
                                assertThat(
                                                Jsoup.parse(
                                                                result.getResponse()
                                                                        .getContentAsString())
                                                        .selectFirst("#name")
                                                        .val())
                                        .isEqualTo("Original"));
        mvc.perform(
                        post("/tasks/{id}/name", id)
                                .with(csrf())
                                .param("name", " Geändert ")
                                .param("priority", "LOW")
                                .param("done", "false")
                                .param("created", "2000-01-01T00:00:00Z")
                                .param("id", "9999"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/tasks"));
        Task actual = repository.findById(id).orElseThrow();
        assertThat(actual.getName()).isEqualTo("Geändert");
        assertThat(actual.isDone()).isTrue();
        assertThat(actual.getPriority()).isEqualTo(Priority.URGENT);
        assertThat(actual.getCreated()).isEqualTo(created);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\u2003", "bad\u0000name"})
    void rejectsInvalidRenameAndRetainsExistingTask(String name) throws Exception {
        Long id =
                repository
                        .saveAndFlush(new Task("Original", Priority.NORMAL, Instant.now()))
                        .getId();
        mvc.perform(post("/tasks/{id}/name", id).with(csrf()).param("name", name))
                .andExpect(status().isBadRequest())
                .andExpect(model().attributeHasFieldErrors("renameForm", "name"))
                .andExpect(content().string(containsString("Aufgabe umbenennen")));
        assertThat(repository.findById(id).orElseThrow().getName()).isEqualTo("Original");
    }

    @Test
    void rejectsOversizedRenameAndPreservesEnteredValue() throws Exception {
        Long id =
                repository
                        .saveAndFlush(new Task("Original", Priority.NORMAL, Instant.now()))
                        .getId();
        String name = "x".repeat(201);
        mvc.perform(post("/tasks/{id}/name", id).with(csrf()).param("name", name))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result ->
                                assertThat(
                                                Jsoup.parse(
                                                                result.getResponse()
                                                                        .getContentAsString())
                                                        .selectFirst("#name")
                                                        .val())
                                        .isEqualTo(name));
        assertThat(repository.findById(id).orElseThrow().getName()).isEqualTo("Original");
    }

    @Test
    void completesAndReopensWithoutChangingOtherFields() throws Exception {
        Instant created = Instant.parse("2026-09-24T12:30:00Z");
        Long id = repository.saveAndFlush(new Task("Original", Priority.LOW, created)).getId();
        for (String done : new String[] {"true", "true", "false"}) {
            mvc.perform(
                            post("/tasks/{id}/done", id)
                                    .with(csrf())
                                    .param("done", done)
                                    .param("name", "Attack")
                                    .param("priority", "URGENT"))
                    .andExpect(status().isSeeOther())
                    .andExpect(redirectedUrl("/tasks"));
            Task actual = repository.findById(id).orElseThrow();
            assertThat(actual.isDone()).isEqualTo(Boolean.parseBoolean(done));
            assertThat(actual.getName()).isEqualTo("Original");
            assertThat(actual.getPriority()).isEqualTo(Priority.LOW);
            assertThat(actual.getCreated()).isEqualTo(created);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "yes", "1", "TRUE", "false,true"})
    void rejectsNonCanonicalStatus(String done) throws Exception {
        Long id =
                repository
                        .saveAndFlush(new Task("Original", Priority.NORMAL, Instant.now()))
                        .getId();
        mvc.perform(post("/tasks/{id}/done", id).with(csrf()).param("done", done))
                .andExpect(status().isBadRequest());
        assertThat(repository.findById(id).orElseThrow().isDone()).isFalse();
    }

    @Test
    void rejectsMissingAndRepeatedStatus() throws Exception {
        Long id =
                repository
                        .saveAndFlush(new Task("Original", Priority.NORMAL, Instant.now()))
                        .getId();
        mvc.perform(post("/tasks/{id}/done", id).with(csrf())).andExpect(status().isBadRequest());
        mvc.perform(post("/tasks/{id}/done", id).with(csrf()).param("done", "true", "false"))
                .andExpect(status().isBadRequest());
        assertThat(repository.findById(id).orElseThrow().isDone()).isFalse();
    }

    @Test
    void missingTaskAndMalformedIdReturnAppropriateErrors() throws Exception {
        mvc.perform(get("/tasks/999999/edit"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Diese Aufgabe wurde nicht gefunden.")));
        mvc.perform(post("/tasks/999999/name").with(csrf()).param("name", "Name"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/tasks/999999/done").with(csrf()).param("done", "true"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks/invalid/edit")).andExpect(status().isBadRequest());
        mvc.perform(post("/tasks/invalid/name").with(csrf()).param("name", "Name"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/tasks/invalid/done").with(csrf()).param("done", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mutationRoutesRequireCsrfAndRejectGet() throws Exception {
        Long id =
                repository
                        .saveAndFlush(new Task("Original", Priority.NORMAL, Instant.now()))
                        .getId();
        for (String route : new String[] {"name", "done", "delete"}) {
            mvc.perform(
                            post("/tasks/{id}/" + route, id)
                                    .param("name", "Attack")
                                    .param("done", "true"))
                    .andExpect(status().isForbidden());
            mvc.perform(
                            post("/tasks/{id}/" + route, id)
                                    .with(csrf().useInvalidToken())
                                    .param("name", "Attack")
                                    .param("done", "true"))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/tasks/{id}/" + route, id)).andExpect(status().isMethodNotAllowed());
        }
        Task actual = repository.findById(id).orElseThrow();
        assertThat(actual.getName()).isEqualTo("Original");
        assertThat(actual.isDone()).isFalse();
    }

    @Test
    void deletesOnlyPathTaskAndRemainsAbsentAfterReload() throws Exception {
        Long id =
                repository.saveAndFlush(new Task("Entfernen", Priority.LOW, Instant.now())).getId();
        Long otherId =
                repository
                        .saveAndFlush(new Task("Behalten", Priority.URGENT, Instant.now()))
                        .getId();
        mvc.perform(get("/tasks/{id}/delete/confirm", id))
                .andExpect(
                        result -> {
                            var page = Jsoup.parse(result.getResponse().getContentAsString());
                            var form = page.selectFirst("form[action='/tasks/" + id + "/delete']");
                            assertThat(form).isNotNull();
                            assertThat(form.select("input[name=_csrf]")).hasSize(1);
                            assertThat(form.selectFirst("button").text())
                                    .isEqualTo("Endgültig löschen");
                        });
        mvc.perform(post("/tasks/{id}/delete", id).with(csrf()).param("id", otherId.toString()))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/tasks"));
        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.findAll()).extracting(Task::getId).containsExactly(otherId);
        mvc.perform(get("/tasks"))
                .andExpect(content().string(not(containsString("Entfernen"))))
                .andExpect(content().string(containsString("Behalten")));
        mvc.perform(post("/tasks/{id}/delete", id).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{id}/edit", id)).andExpect(status().isNotFound());
    }

    @Test
    void deletingLastTaskRestoresEmptyState() throws Exception {
        Long id =
                repository.saveAndFlush(new Task("Letzte", Priority.NORMAL, Instant.now())).getId();
        mvc.perform(post("/tasks/{id}/delete", id).with(csrf())).andExpect(status().isSeeOther());
        mvc.perform(get("/tasks"))
                .andExpect(content().string(containsString("Noch keine Aufgaben")));
        assertThat(repository.count()).isZero();
    }

    @Test
    void rejectsUnknownAndMalformedDeleteIds() throws Exception {
        mvc.perform(post("/tasks/999999/delete").with(csrf())).andExpect(status().isNotFound());
        mvc.perform(post("/tasks/invalid/delete").with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void confirmationViewsDoNotChangeDataAndPreviewRequiresCsrf() throws Exception {
        Long id =
                repository.saveAndFlush(new Task("Vorher", Priority.NORMAL, Instant.now())).getId();
        mvc.perform(get("/tasks/{id}/delete/confirm", id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Endgültig löschen")));
        assertThat(repository.existsById(id)).isTrue();
        mvc.perform(post("/tasks/{id}/name/preview", id).with(csrf()).param("name", "Nachher"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vorher")))
                .andExpect(content().string(containsString("Nachher")));
        assertThat(repository.findById(id).orElseThrow().getName()).isEqualTo("Vorher");
        mvc.perform(post("/tasks/{id}/name/preview", id).param("name", "Nachher"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/tasks/{id}/name/preview", id).with(csrf()).param("name", " "))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/tasks/999999/delete/confirm")).andExpect(status().isNotFound());
        mvc.perform(post("/tasks/999999/name/preview").with(csrf()).param("name", "Nachher"))
                .andExpect(status().isNotFound());
    }

    @Test
    void statusRedirectOffersPreviousStateForUndo() throws Exception {
        Long id =
                repository.saveAndFlush(new Task("Status", Priority.NORMAL, Instant.now())).getId();
        var result =
                mvc.perform(post("/tasks/{id}/done", id).with(csrf()).param("done", "true"))
                        .andExpect(status().isSeeOther())
                        .andReturn();
        mvc.perform(get("/tasks").flashAttrs(result.getFlashMap()))
                .andExpect(content().string(containsString("Rückgängig")));
        var previous =
                (de.sinanucar.taskmanagement.task.application.TaskData)
                        result.getFlashMap().get("undoTask");
        assertThat(previous.done()).isFalse();
        mvc.perform(
                        post("/tasks/{id}/done", id)
                                .with(csrf())
                                .param("done", Boolean.toString(previous.done()))
                                .param("undo", "true"))
                .andExpect(status().isSeeOther())
                .andExpect(flash().attributeCount(0));
        mvc.perform(get("/tasks"))
                .andExpect(content().string(not(containsString("Rückgängig"))))
                .andExpect(content().string(containsString("Status")));
        assertThat(repository.findById(id).orElseThrow().isDone()).isFalse();
    }
}
