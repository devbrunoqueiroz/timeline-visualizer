package com.chronicle.application.character.list;

import com.chronicle.domain.character.Character;

import java.math.BigDecimal;

public record CharacterSummaryView(
        String id,
        String timelineId,
        String linkedTimelineId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {
    public static CharacterSummaryView from(Character c) {
        return new CharacterSummaryView(
                c.getId().toString(), c.getTimelineId().toString(),
                c.getLinkedTimelineId() != null ? c.getLinkedTimelineId().toString() : null,
                c.getName(), c.getDescription(),
                c.getStartPosition(), c.getEndPosition()
        );
    }
}
