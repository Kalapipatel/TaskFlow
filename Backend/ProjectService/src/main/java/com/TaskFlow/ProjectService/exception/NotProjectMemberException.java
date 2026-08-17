package com.TaskFlow.ProjectService.exception;

import org.springframework.http.HttpStatus;

public class NotProjectMemberException extends ApiException {
    public NotProjectMemberException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}