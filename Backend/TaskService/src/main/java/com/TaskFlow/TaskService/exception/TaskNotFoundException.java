package com.TaskFlow.TaskService.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TaskNotFoundException extends ApiException {
    public TaskNotFoundException(UUID taskId) {
        super(HttpStatus.NOT_FOUND, "Task not found: " + taskId);
    }
}