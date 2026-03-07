package com.chronicle.domain.connection;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record ConnectionId(UUID value) {

    public ConnectionId {
        Objects.requireNonNull(value, "ConnectionId cannot be null");
    }

    public static ConnectionId generate() {
        return new ConnectionId(UUID.randomUUID());
    }

    public static ConnectionId of(String value) {
        try {
            return new ConnectionId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid ConnectionId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
