package com.chronicle.domain.user;

import com.chronicle.application.shared.ApplicationException;

public class UserAlreadyExistsException extends ApplicationException {
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}
