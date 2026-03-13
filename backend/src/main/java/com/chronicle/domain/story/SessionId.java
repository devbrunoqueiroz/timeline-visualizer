package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record SessionId(UUID value) {

    public SessionId {
        Objects.requireNonNull(value, "SessionId cannot be null");
    }

    public static SessionId generate() {
        return new SessionId(UUID.randomUUID());
    }

    public static SessionId of(String value) {
        try {
            return new SessionId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid SessionId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
