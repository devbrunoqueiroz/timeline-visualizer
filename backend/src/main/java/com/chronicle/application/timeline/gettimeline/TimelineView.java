package com.chronicle.application.timeline.gettimeline;

import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineVisibility;

import java.time.Instant;
import java.util.List;

public record TimelineView(String id, String name, String description,
                            TimelineVisibility visibility, List<TimelineEventView> events,
                            Instant createdAt, Instant updatedAt) {

    public static TimelineView from(Timeline timeline) {
        var events = timeline.getEvents().stream()
                .map(TimelineEventView::from)
                .toList();
        return new TimelineView(
                timeline.getId().value().toString(),
                timeline.getName(),
                timeline.getDescription(),
                timeline.getVisibility(),
                events,
                timeline.getCreatedAt(),
                timeline.getUpdatedAt()
        );
    }
}
