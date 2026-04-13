package com.rares.budget_management_app.common.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(ErrorMessage error, Object... args) {
        super(error.format(args));
    }
}
