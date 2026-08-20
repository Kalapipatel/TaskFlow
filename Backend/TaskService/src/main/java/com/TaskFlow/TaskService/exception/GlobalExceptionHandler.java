package com.TaskFlow.TaskService.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        String requestId = getOrGenerateRequestId(request);
        log.warn("[{}] {} {} - {} {}", requestId, request.getMethod(), request.getRequestURI(), ex.getStatus().value(), ex.getMessage());

        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponse.of(ex.getStatus(), ex.getMessage(), request.getRequestURI(), requestId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String requestId = getOrGenerateRequestId(request);
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        log.warn("[{}] Validation failed on {} {}: {}", requestId, request.getMethod(), request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(ErrorResponse.validation(request.getRequestURI(), requestId, fieldErrors));
    }

    // Prevents 500s when concurrent requests hit the unique (project_id, task_number) constraint
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String requestId = getOrGenerateRequestId(request);
        log.error("[{}] Data integrity violation: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, "A resource with these details already exists or violates constraints", request.getRequestURI(), requestId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        String requestId = getOrGenerateRequestId(request);
        log.error("[{}] Unexpected error on {} {}: ", requestId, request.getMethod(), request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Reference ID: " + requestId, request.getRequestURI(), requestId));
    }

    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return (requestId != null && !requestId.isBlank()) ? requestId : UUID.randomUUID().toString();
    }
}