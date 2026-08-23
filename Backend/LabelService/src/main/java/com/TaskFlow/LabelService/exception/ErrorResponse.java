package com.TaskFlow.LabelService.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String requestId;
    private Map<String, String> fieldErrors;

    public static ErrorResponse of(
            HttpStatus status,
            String message,
            String path,
            String requestId
    ) {
        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(status.name())
                .message(message)
                .path(path)
                .requestId(requestId)
                .build();
    }

    public static ErrorResponse validation(
            String path,
            String requestId,
            Map<String, String> fieldErrors
    ) {
        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message("Request validation failed")
                .path(path)
                .requestId(requestId)
                .fieldErrors(fieldErrors)
                .build();
    }
}