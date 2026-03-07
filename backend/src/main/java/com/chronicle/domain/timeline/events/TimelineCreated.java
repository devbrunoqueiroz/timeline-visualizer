package com.chronicle.domain.timeline.events;

import com.chronicle.domain.shared.DomainEvent;
import com.chronicle.domain.timeline.TimelineId;

import java.time.Instant;

public record TimelineCreated(TimelineId timelineId, String name, Instant occurredOn) implements DomainEvent {

    public TimelineCreated(TimelineId timelineId, String name) {
        this(timelineId, name, Instant.now());
    }
}
