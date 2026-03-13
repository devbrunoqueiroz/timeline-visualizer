package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record SceneId(UUID value) {

    public SceneId {
        Objects.requireNonNull(value, "SceneId cannot be null");
    }

    public static SceneId generate() {
        return new SceneId(UUID.randomUUID());
    }

    public static SceneId of(String value) {
        try {
            return new SceneId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid SceneId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
