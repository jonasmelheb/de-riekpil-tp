package de.riekpil.blog.task;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api/tasks/demo")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Void> update(@RequestBody JsonNode payload,
                                       UriComponentsBuilder uriComponentsBuilder) {
        Long taskIs = taskService.createTask(payload.get("taskTitle").asText());

        return ResponseEntity
                .created(uriComponentsBuilder.path("/api/tasks/demo/{taskIs}").build(taskIs))
                .build();
    }

    @DeleteMapping("{taskId}")
    @RolesAllowed("ADMIN")
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }
}
