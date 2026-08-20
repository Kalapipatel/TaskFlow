package com.TaskFlow.TaskService.controller;


import com.TaskFlow.TaskService.dto.*;
import com.TaskFlow.TaskService.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @RequestHeader("X-User-Id") UUID requesterId) {

        // The requesterId is injected directly as the reporterId by the controller
        // to prevent users from spoofing who reported the task.
        request.setReporterId(requesterId);

        TaskResponse response = taskService.createTask(request, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskUpdateRequest request) {

        TaskResponse response = taskService.updateTask(requesterId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/tasks/board")
    public ResponseEntity<BoardResponse> getKanbanBoard(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int doneOffset,
            @RequestParam(defaultValue = "20") int doneLimit) {

        BoardResponse response = taskService.getKanbanBoard(projectId, doneOffset, doneLimit);
        return ResponseEntity.ok(response);
    }

}