package com.TaskFlow.ProjectService.exception;

import org.springframework.http.HttpStatus;

public class ProjectKeyAlreadyExistsException extends ApiException {
    public ProjectKeyAlreadyExistsException(String key) {
        super(HttpStatus.CONFLICT, "Project key already exists: " + key);
    }
}