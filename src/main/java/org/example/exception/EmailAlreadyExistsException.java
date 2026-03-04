package org.example.exception;

public class EmailAlreadyExistsException extends RegistrationException {
    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
