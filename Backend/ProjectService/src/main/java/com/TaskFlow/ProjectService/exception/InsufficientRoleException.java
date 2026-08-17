package com.TaskFlow.ProjectService.exception;

import org.springframework.http.HttpStatus;

public class InsufficientRoleException extends ApiException {
    public InsufficientRoleException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}