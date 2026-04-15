package com.rares.budget_management_app.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private Error error;

    public DuplicateResourceException(Error error, Object... args) {
        super(error.format(args));
        this.error = error;
    }
}
