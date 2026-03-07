package com.chronicle.domain.timeline;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record TimelineId(UUID value) {

    public TimelineId {
        Objects.requireNonNull(value, "TimelineId cannot be null");
    }

    public static TimelineId generate() {
        return new TimelineId(UUID.randomUUID());
    }

    public static TimelineId of(String value) {
        try {
            return new TimelineId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid TimelineId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
