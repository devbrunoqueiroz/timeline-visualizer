package com.chronicle.domain.character;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record CharacterEventId(UUID value) {

    public CharacterEventId {
        Objects.requireNonNull(value, "CharacterEventId cannot be null");
    }

    public static CharacterEventId generate() {
        return new CharacterEventId(UUID.randomUUID());
    }

    public static CharacterEventId of(String value) {
        try {
            return new CharacterEventId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid CharacterEventId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
