package com.TaskFlow.TaskService.controller;


import com.TaskFlow.TaskService.dto.*;
import com.TaskFlow.TaskService.enums.*;
import com.TaskFlow.TaskService.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskDetail(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId) {
        // TODO (Phase 3): Validate requester membership to project via Feign
        return ResponseEntity.ok(taskService.getTaskDetail(taskId));
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

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Page<TaskSummary>> searchTasks(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
            @RequestParam(required = false) UUID labelId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TaskSummary> response = taskService.searchTasks(
                projectId, status, priority, assigneeId, search, dueBefore, labelId, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/labels")
    public ResponseEntity<Void> attachLabels(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AttachLabelsRequest request) {

        taskService.attachLabels(requesterId, taskId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}/labels/{labelId}")
    public ResponseEntity<Void> removeLabel(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId,
            @PathVariable UUID labelId) {

        taskService.removeLabel(requesterId, taskId, labelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId) {

        taskService.deleteTask(requesterId, taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistory(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId) {
        // TODO (Phase 3): Validate requester membership to project via Feign
        return ResponseEntity.ok(taskService.getTaskHistory(taskId));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(requesterId, taskId, request));
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(taskService.assignTask(requesterId, taskId, request));
    }

    @DeleteMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> unassignTask(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.unassignTask(requesterId, taskId));
    }
}