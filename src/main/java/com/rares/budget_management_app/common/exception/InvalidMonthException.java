package com.rares.budget_management_app.common.exception;

import lombok.Getter;

@Getter
public class InvalidMonthException extends RuntimeException {
    private Error error;

    public InvalidMonthException(Error error, Object... args) {
        super(error.format(args));
        this.error = error;
    }
}
