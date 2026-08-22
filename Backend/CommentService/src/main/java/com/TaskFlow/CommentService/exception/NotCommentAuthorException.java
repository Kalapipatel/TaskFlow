package com.TaskFlow.CommentService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotCommentAuthorException extends RuntimeException {
    public NotCommentAuthorException(String message) {
        super(message);
    }
}