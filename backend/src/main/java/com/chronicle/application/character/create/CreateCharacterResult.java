package com.chronicle.application.character.create;

import com.chronicle.domain.character.Character;

import java.math.BigDecimal;

public record CreateCharacterResult(
        String id,
        String timelineId,
        String linkedTimelineId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {
    public static CreateCharacterResult from(Character c) {
        return new CreateCharacterResult(
                c.getId().toString(), c.getTimelineId().toString(),
                c.getLinkedTimelineId() != null ? c.getLinkedTimelineId().toString() : null,
                c.getName(), c.getDescription(),
                c.getStartPosition(), c.getEndPosition()
        );
    }
}
