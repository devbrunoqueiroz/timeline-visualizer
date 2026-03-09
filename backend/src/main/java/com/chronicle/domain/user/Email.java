package com.chronicle.domain.user;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;

public record Email(String value) {

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        var normalized = value.strip().toLowerCase();
        if (!normalized.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new DomainException("Invalid email format: " + value);
        }
        value = normalized;
    }

    @Override
    public String toString() {
        return value;
    }
}
