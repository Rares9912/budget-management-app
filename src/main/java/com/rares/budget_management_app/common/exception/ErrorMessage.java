package com.rares.budget_management_app.common.exception;

public enum ErrorMessage {
    CATEGORY_NOT_FOUND("Categoria '%s' nu există"),
    CATEGORY_ALREADY_EXISTS("Categoria '%s' există deja"),
    BUDGET_NOT_FOUND("Bugetul categoriei '%s' din %s/%d nu există"),
    BUDGET_ALREADY_EXISTS("Bugetul categoriei '%s' din %s/%d exista deja"),
    PAST_BUDGET_MODIFICATION("Nu poti modifica bugetul din data de %s/%d"),
    USER_NOT_FOUND("Userul '%s' nu există");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}