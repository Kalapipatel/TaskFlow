package com.TaskFlow.CommentService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CommentAlreadyDeletedException extends RuntimeException {
    public CommentAlreadyDeletedException(String message) {
        super(message);
    }
}