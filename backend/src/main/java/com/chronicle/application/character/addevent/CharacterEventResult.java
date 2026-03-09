package com.chronicle.application.character.addevent;

import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineEvent;

public record CharacterEventResult(
        String id,
        String title,
        String contentText,
        String contentType,
        TemporalPosition temporalPosition,
        int displayOrder
) {
    public static CharacterEventResult from(TimelineEvent e) {
        return new CharacterEventResult(
                e.getId().value().toString(), e.getTitle(),
                e.getContent().text(), e.getContent().type().name(),
                e.getTemporalPosition(), e.getDisplayOrder()
        );
    }
}
