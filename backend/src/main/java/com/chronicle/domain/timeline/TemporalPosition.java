package com.chronicle.domain.timeline;

import com.chronicle.domain.shared.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record TemporalPosition(BigDecimal position, String label, String calendarSystem) {

    public static final String GREGORIAN = "GREGORIAN";
    public static final String CUSTOM = "CUSTOM";

    public TemporalPosition {
        Objects.requireNonNull(position, "position cannot be null");
        if (label == null || label.isBlank()) {
            throw new DomainException("Temporal position label cannot be blank");
        }
        calendarSystem = calendarSystem != null ? calendarSystem : CUSTOM;
    }

    public static TemporalPosition gregorian(Instant instant) {
        Objects.requireNonNull(instant, "instant cannot be null");
        return new TemporalPosition(
                BigDecimal.valueOf(instant.toEpochMilli()),
                instant.toString(),
                GREGORIAN
        );
    }

    public static TemporalPosition custom(BigDecimal position, String label) {
        return new TemporalPosition(position, label, CUSTOM);
    }

    public static TemporalPosition custom(long position, String label) {
        return new TemporalPosition(BigDecimal.valueOf(position), label, CUSTOM);
    }

    public boolean isGregorian() {
        return GREGORIAN.equals(calendarSystem);
    }

    public Instant toInstant() {
        if (!isGregorian()) {
            throw new DomainException("Cannot convert non-Gregorian temporal position to Instant");
        }
        return Instant.ofEpochMilli(position.longValue());
    }
}
