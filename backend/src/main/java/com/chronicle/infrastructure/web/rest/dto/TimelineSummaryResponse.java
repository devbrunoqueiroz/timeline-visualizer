package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.timeline.listtimelines.TimelineSummaryView;
import com.chronicle.domain.timeline.TimelineVisibility;

import java.time.Instant;

public record TimelineSummaryResponse(String id, String name, String description,
                                       TimelineVisibility visibility, int eventCount,
                                       Instant createdAt, Instant updatedAt) {

    public static TimelineSummaryResponse from(TimelineSummaryView view) {
        return new TimelineSummaryResponse(
                view.id(), view.name(), view.description(),
                view.visibility(), view.eventCount(), view.createdAt(), view.updatedAt()
        );
    }
}
