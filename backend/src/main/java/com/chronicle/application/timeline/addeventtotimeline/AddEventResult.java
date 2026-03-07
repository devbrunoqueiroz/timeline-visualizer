package com.chronicle.application.timeline.addeventtotimeline;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineEvent;

public record AddEventResult(String id, String title, String contentText,
                              ContentType contentType, TemporalPosition temporalPosition,
                              int displayOrder) {

    public static AddEventResult from(TimelineEvent event) {
        return new AddEventResult(
                event.getId().value().toString(),
                event.getTitle(),
                event.getContent().text(),
                event.getContent().type(),
                event.getTemporalPosition(),
                event.getDisplayOrder()
        );
    }
}
