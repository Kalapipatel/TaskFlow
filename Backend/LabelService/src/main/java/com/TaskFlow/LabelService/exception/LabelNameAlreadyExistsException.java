package com.TaskFlow.LabelService.exception;

import org.springframework.http.HttpStatus;

public class LabelNameAlreadyExistsException extends ApiException {

    public LabelNameAlreadyExistsException(String name) {
        super(
                HttpStatus.CONFLICT,
                "A label named '" + name + "' already exists in this project"
        );
    }
}