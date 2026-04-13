package com.rares.budget_management_app.common.exception;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String email) {
        super("Email-ul este deja folosit: " + email);
    }
}
