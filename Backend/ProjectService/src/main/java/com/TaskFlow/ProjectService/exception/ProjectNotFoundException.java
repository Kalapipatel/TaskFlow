package com.TaskFlow.ProjectService.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProjectNotFoundException extends ApiException {
    public ProjectNotFoundException(UUID projectId) {
        super(HttpStatus.NOT_FOUND, "Project not found: " + projectId);
    }
}
