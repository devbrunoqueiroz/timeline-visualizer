package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.character.get.CharacterView;

import java.math.BigDecimal;
import java.util.List;

public record CharacterResponse(
        String id,
        String timelineId,
        String linkedTimelineId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition,
        List<CharacterEventResponse> events
) {
    public static CharacterResponse from(CharacterView v) {
        return new CharacterResponse(v.id(), v.timelineId(), v.linkedTimelineId(),
                v.name(), v.description(), v.startPosition(), v.endPosition(),
                v.events().stream().map(e -> new CharacterEventResponse(
                        e.id(), e.title(), e.contentText(), e.contentType(),
                        e.temporalPosition(), e.temporalLabel(), e.calendarSystem(), e.displayOrder()
                )).toList()
        );
    }
}
