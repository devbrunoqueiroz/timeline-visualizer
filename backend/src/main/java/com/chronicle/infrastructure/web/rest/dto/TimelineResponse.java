package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.timeline.gettimeline.TimelineView;
import com.chronicle.domain.timeline.TimelineVisibility;

import java.time.Instant;
import java.util.List;

public record TimelineResponse(String id, String name, String description,
                                TimelineVisibility visibility, List<TimelineEventResponse> events,
                                Instant createdAt, Instant updatedAt) {

    public static TimelineResponse from(TimelineView view) {
        var events = view.events().stream()
                .map(TimelineEventResponse::from)
                .toList();
        return new TimelineResponse(
                view.id(), view.name(), view.description(),
                view.visibility(), events, view.createdAt(), view.updatedAt()
        );
    }
}
