package com.TaskFlow.LabelService.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

public class InvalidLabelException extends ApiException {

    public InvalidLabelException(
            UUID projectId,
            List<UUID> invalidLabelIds
    ) {
        super(
                HttpStatus.BAD_REQUEST,
                "Label IDs " + invalidLabelIds
                        + " do not belong to project " + projectId
        );
    }
}
