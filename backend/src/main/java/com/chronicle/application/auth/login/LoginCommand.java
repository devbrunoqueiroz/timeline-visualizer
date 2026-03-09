package com.chronicle.application.auth.login;

import java.util.Objects;

public record LoginCommand(String email, String password) {
    public LoginCommand {
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(password, "Password is required");
    }
}
