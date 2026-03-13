package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record StoryId(UUID value) {

    public StoryId {
        Objects.requireNonNull(value, "StoryId cannot be null");
    }

    public static StoryId generate() {
        return new StoryId(UUID.randomUUID());
    }

    public static StoryId of(String value) {
        try {
            return new StoryId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid StoryId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
