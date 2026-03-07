package com.chronicle.application.timeline.updateevent;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineEvent;

public record UpdateEventResult(String id, String title, String contentText,
                                 ContentType contentType, TemporalPosition temporalPosition,
                                 int displayOrder) {

    public static UpdateEventResult from(TimelineEvent event) {
        return new UpdateEventResult(
                event.getId().value().toString(),
                event.getTitle(),
                event.getContent().text(),
                event.getContent().type(),
                event.getTemporalPosition(),
                event.getDisplayOrder()
        );
    }
}
