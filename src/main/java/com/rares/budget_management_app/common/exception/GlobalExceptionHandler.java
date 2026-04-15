package com.rares.budget_management_app.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(Error.INVALID_CREDENTIALS, Error.INVALID_CREDENTIALS.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        return buildResponse(ex.getError(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex) {
        return buildResponse(ex.getError(), ex.getMessage());
    }

    @ExceptionHandler(PastBudgetModificationException.class)
    public ResponseEntity<ErrorResponse> handlePastBudgetModification(
            PastBudgetModificationException ex) {
        return buildResponse(ex.getError(), ex.getMessage());
    }

    @ExceptionHandler(InvalidMonthException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMonth(
            InvalidMonthException ex) {
        return buildResponse(ex.getError(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(Error.VALIDATION_ERROR, message);
    }

    private ResponseEntity<ErrorResponse> buildResponse(Error error, String errorMessage) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(error.getCode())
                .message(errorMessage)
                .httpStatus(error.getHttpStatus().value())
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(errorResponse);
    }
}
