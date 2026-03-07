package com.chronicle.application.timeline.createtimeline;

import com.chronicle.domain.timeline.Timeline;

public record CreateTimelineResult(String id) {

    public static CreateTimelineResult from(Timeline timeline) {
        return new CreateTimelineResult(timeline.getId().value().toString());
    }
}
