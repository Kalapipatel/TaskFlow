package com.TaskFlow.CommentService.exception;

import org.springframework.http.HttpStatus;

public class NestedReplyNotAllowedException extends ApiException {
    public NestedReplyNotAllowedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}