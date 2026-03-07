package com.chronicle.application.timeline.updatetimeline;

import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineVisibility;

import java.time.Instant;

public record UpdateTimelineResult(String id, String name, String description,
                                    TimelineVisibility visibility, Instant updatedAt) {

    public static UpdateTimelineResult from(Timeline timeline) {
        return new UpdateTimelineResult(
                timeline.getId().value().toString(),
                timeline.getName(),
                timeline.getDescription(),
                timeline.getVisibility(),
                timeline.getUpdatedAt()
        );
    }
}
