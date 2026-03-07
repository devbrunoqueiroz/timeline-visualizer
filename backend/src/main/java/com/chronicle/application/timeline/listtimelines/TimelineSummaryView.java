package com.chronicle.application.timeline.listtimelines;

import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineVisibility;

import java.time.Instant;

public record TimelineSummaryView(String id, String name, String description,
                                   TimelineVisibility visibility, int eventCount,
                                   Instant createdAt, Instant updatedAt) {

    public static TimelineSummaryView from(Timeline timeline) {
        return new TimelineSummaryView(
                timeline.getId().value().toString(),
                timeline.getName(),
                timeline.getDescription(),
                timeline.getVisibility(),
                timeline.getEvents().size(),
                timeline.getCreatedAt(),
                timeline.getUpdatedAt()
        );
    }
}
