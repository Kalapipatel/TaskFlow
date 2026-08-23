package com.TaskFlow.LabelService.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class LabelNotFoundException extends ApiException {

    public LabelNotFoundException(UUID labelId) {
        super(HttpStatus.NOT_FOUND, "Label not found: " + labelId);
    }
}
