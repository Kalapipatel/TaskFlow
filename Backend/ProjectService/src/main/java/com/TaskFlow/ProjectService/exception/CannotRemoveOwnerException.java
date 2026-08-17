package com.TaskFlow.ProjectService.exception;

import org.springframework.http.HttpStatus;

public class CannotRemoveOwnerException extends ApiException {
    public CannotRemoveOwnerException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}