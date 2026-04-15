package com.rares.budget_management_app.common.exception;

import org.springframework.http.HttpStatus;

public enum Error {
    // Auth — 1000+
    EMAIL_ALREADY_EXISTS(1001, "Email already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(1002, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),

    // Category — 2000+
    CATEGORY_NOT_FOUND(2001, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(2002, "Category already exists", HttpStatus.CONFLICT),

    // Budget — 3000+
    BUDGET_NOT_FOUND(3001, "Budget not found", HttpStatus.NOT_FOUND),
    BUDGET_ALREADY_EXISTS(3002, "Budget already exists", HttpStatus.CONFLICT),
    PAST_BUDGET_MODIFICATION(3003, "You cannot modify a budget from the past", HttpStatus.FORBIDDEN),

    // Expense — 4000+
    EXPENSE_NOT_FOUND(4001, "Expense not found", HttpStatus.NOT_FOUND),

    // General — 5000+
    INVALID_MONTH(5001, "Invalid month", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(5002, "Invalid data", HttpStatus.BAD_REQUEST),
    CURRENCY_CONVERSION_FAILED(5003, "Currency conversion failed", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    Error(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }

    public String format(Object... args) {
        return String.format(message, args);
    }
}