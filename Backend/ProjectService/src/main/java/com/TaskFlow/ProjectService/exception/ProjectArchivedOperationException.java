package com.TaskFlow.ProjectService.exception;

import org.springframework.http.HttpStatus;

public class ProjectArchivedOperationException extends ApiException {
    public ProjectArchivedOperationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}