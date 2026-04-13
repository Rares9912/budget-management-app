package com.rares.budget_management_app.common.exception;


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(ErrorMessage error, Object... args) {
        super(error.format(args));
    }
}
