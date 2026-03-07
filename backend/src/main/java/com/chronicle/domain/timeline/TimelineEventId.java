package com.chronicle.domain.timeline;

import com.chronicle.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

public record TimelineEventId(UUID value) {

    public TimelineEventId {
        Objects.requireNonNull(value, "TimelineEventId cannot be null");
    }

    public static TimelineEventId generate() {
        return new TimelineEventId(UUID.randomUUID());
    }

    public static TimelineEventId of(String value) {
        try {
            return new TimelineEventId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid TimelineEventId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
