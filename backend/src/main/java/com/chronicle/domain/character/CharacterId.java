package com.chronicle.domain.character;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record CharacterId(UUID value) {

    public CharacterId {
        Objects.requireNonNull(value, "CharacterId cannot be null");
    }

    public static CharacterId generate() {
        return new CharacterId(UUID.randomUUID());
    }

    public static CharacterId of(String value) {
        try {
            return new CharacterId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid CharacterId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
