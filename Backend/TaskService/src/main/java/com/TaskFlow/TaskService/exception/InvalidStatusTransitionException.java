package com.TaskFlow.TaskService.exception;

import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends ApiException {
    public InvalidStatusTransitionException(String from, String to, String allowed) {
        super(HttpStatus.BAD_REQUEST,
                "Cannot transition task from " + from + " to " + to +
                        ". Allowed transitions: " + allowed);
    }
}