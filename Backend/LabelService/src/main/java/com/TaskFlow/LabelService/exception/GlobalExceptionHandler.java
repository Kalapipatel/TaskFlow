package com.TaskFlow.LabelService.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        String requestId = getOrGenerateRequestId(request);

        log.warn(
                "[{}] {} {} - {} {}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                exception.getStatus().value(),
                exception.getMessage()
        );

        ErrorResponse response = ErrorResponse.of(
                exception.getStatus(),
                exception.getMessage(),
                request.getRequestURI(),
                requestId
        );

        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String requestId = getOrGenerateRequestId(request);
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        fieldErrors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        return ResponseEntity.badRequest().body(
                ErrorResponse.validation(
                        request.getRequestURI(),
                        requestId,
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return badRequest(
                "Request body is malformed or contains an invalid value",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return badRequest(
                "Invalid value supplied for '" + exception.getName() + "'",
                request
        );
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleRequestBinding(
            ServletRequestBindingException exception,
            HttpServletRequest request
    ) {
        return badRequest("A required request header is missing or invalid", request);
    }

    /*
     * Handles the concurrent-request race where two creates pass the application
     * uniqueness check but one loses against uk_labels_project_name.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        String requestId = getOrGenerateRequestId(request);

        log.warn(
                "[{}] Label data constraint violation on {} {}",
                requestId,
                request.getMethod(),
                request.getRequestURI()
        );

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT,
                "A label with this name already exists in the project",
                request.getRequestURI(),
                requestId
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        String requestId = getOrGenerateRequestId(request);

        log.error(
                "[{}] Unexpected error on {} {}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Reference ID: " + requestId,
                request.getRequestURI(),
                requestId
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private ResponseEntity<ErrorResponse> badRequest(
            String message,
            HttpServletRequest request
    ) {
        String requestId = getOrGenerateRequestId(request);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI(),
                requestId
        );

        return ResponseEntity.badRequest().body(response);
    }

    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");

        return requestId == null || requestId.isBlank()
                ? UUID.randomUUID().toString()
                : requestId;
    }
}