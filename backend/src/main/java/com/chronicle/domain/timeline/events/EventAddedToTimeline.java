package com.chronicle.domain.timeline.events;

import com.chronicle.domain.shared.DomainEvent;
import com.chronicle.domain.timeline.TimelineEventId;
import com.chronicle.domain.timeline.TimelineId;

import java.time.Instant;

public record EventAddedToTimeline(TimelineId timelineId, TimelineEventId eventId, Instant occurredOn)
        implements DomainEvent {

    public EventAddedToTimeline(TimelineId timelineId, TimelineEventId eventId) {
        this(timelineId, eventId, Instant.now());
    }
}
