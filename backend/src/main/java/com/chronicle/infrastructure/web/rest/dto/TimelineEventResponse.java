package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.timeline.gettimeline.TimelineEventView;
import com.chronicle.domain.timeline.ContentType;

import java.math.BigDecimal;

public record TimelineEventResponse(String id, String title, String contentText,
                                     ContentType contentType,
                                     BigDecimal temporalPosition,
                                     String temporalLabel,
                                     String calendarSystem,
                                     int displayOrder) {

    public static TimelineEventResponse from(TimelineEventView view) {
        return new TimelineEventResponse(
                view.id(), view.title(), view.contentText(), view.contentType(),
                view.temporalPosition().position(),
                view.temporalPosition().label(),
                view.temporalPosition().calendarSystem(),
                view.displayOrder()
        );
    }
}
