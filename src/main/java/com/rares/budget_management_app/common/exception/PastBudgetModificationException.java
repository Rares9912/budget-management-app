package com.rares.budget_management_app.common.exception;

public class PastBudgetModificationException extends RuntimeException {
    public PastBudgetModificationException(String message) {

        super(message);
    }

    public PastBudgetModificationException(ErrorMessage error, Object... args) {
        super(error.format(args));
    }
}
