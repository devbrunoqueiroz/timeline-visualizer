package com.chronicle.domain.timeline;

import com.chronicle.domain.shared.DomainException;

public class TimelineNotFoundException extends DomainException {

    public TimelineNotFoundException(TimelineId id) {
        super("Timeline not found: " + id.value());
    }
}
