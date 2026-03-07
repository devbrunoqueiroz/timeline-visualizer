package com.chronicle.application.timeline.gettimeline;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineEvent;

public record TimelineEventView(String id, String title, String contentText,
                                 ContentType contentType, TemporalPosition temporalPosition,
                                 int displayOrder) {

    public static TimelineEventView from(TimelineEvent event) {
        return new TimelineEventView(
                event.getId().value().toString(),
                event.getTitle(),
                event.getContent().text(),
                event.getContent().type(),
                event.getTemporalPosition(),
                event.getDisplayOrder()
        );
    }
}
