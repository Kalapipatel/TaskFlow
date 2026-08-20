package com.TaskFlow.CommentService.exception;

import org.springframework.http.HttpStatus;

public class ParentCommentTaskMismatchException extends ApiException {
    public ParentCommentTaskMismatchException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}