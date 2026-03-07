package com.chronicle.application.timeline.createtimeline;

import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineVisibility;

public record CreateTimelineResult(String id, String name, String description, TimelineVisibility visibility) {

    public static CreateTimelineResult from(Timeline timeline) {
        return new CreateTimelineResult(
                timeline.getId().value().toString(),
                timeline.getName(),
                timeline.getDescription(),
                timeline.getVisibility()
        );
    }
}
