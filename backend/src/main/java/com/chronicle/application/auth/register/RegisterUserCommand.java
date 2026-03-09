package com.chronicle.application.auth.register;

import java.util.Objects;

public record RegisterUserCommand(String email, String password) {
    public RegisterUserCommand {
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(password, "Password is required");
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
