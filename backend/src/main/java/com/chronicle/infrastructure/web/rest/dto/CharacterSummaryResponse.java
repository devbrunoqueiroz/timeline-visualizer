package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.character.list.CharacterSummaryView;

import java.math.BigDecimal;

public record CharacterSummaryResponse(
        String id,
        String timelineId,
        String linkedTimelineId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {
    public static CharacterSummaryResponse from(CharacterSummaryView v) {
        return new CharacterSummaryResponse(v.id(), v.timelineId(), v.linkedTimelineId(),
                v.name(), v.description(), v.startPosition(), v.endPosition());
    }
}
