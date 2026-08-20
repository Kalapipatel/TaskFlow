package com.TaskFlow.TaskService.controller;

import com.TaskFlow.TaskService.dto.BatchTasksRequest;
import com.TaskFlow.TaskService.dto.TaskSummary;
import com.TaskFlow.TaskService.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Internal controller meant ONLY for cross-service Feign calls[cite: 2].
 * This is whitelisted in SecurityConfig and not exposed through the API Gateway[cite: 2].
 */
@RestController
@RequestMapping("/internal/tasks")
@RequiredArgsConstructor
public class InternalTaskController {

    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskSummary> getTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getInternalTask(taskId));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TaskSummary>> getTasksBatch(@Valid @RequestBody BatchTasksRequest request) {
        return ResponseEntity.ok(taskService.getTasksBatch(request.getTaskIds()));
    }
}