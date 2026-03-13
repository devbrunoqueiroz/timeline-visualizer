package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

public record NarrativePriority(int value) implements Comparable<NarrativePriority> {

    public NarrativePriority {
        if (value < 0 || value > 100) {
            throw new DomainException("Priority must be between 0 and 100, got: " + value);
        }
    }

    public static NarrativePriority of(int value) {
        return new NarrativePriority(value);
    }

    public static NarrativePriority defaultPriority() {
        return new NarrativePriority(50);
    }

    @Override
    public int compareTo(NarrativePriority other) {
        return Integer.compare(this.value, other.value);
    }
}
