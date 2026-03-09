package com.chronicle.application.character.get;

import com.chronicle.domain.character.Character;
import com.chronicle.domain.timeline.TimelineEvent;

import java.math.BigDecimal;
import java.util.List;

public record CharacterView(
        String id,
        String timelineId,
        String linkedTimelineId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition,
        List<CharacterEventView> events
) {
    public record CharacterEventView(
            String id,
            String title,
            String contentText,
            String contentType,
            BigDecimal temporalPosition,
            String temporalLabel,
            String calendarSystem,
            int displayOrder
    ) {
        public static CharacterEventView from(TimelineEvent e) {
            return new CharacterEventView(
                    e.getId().value().toString(), e.getTitle(),
                    e.getContent().text(), e.getContent().type().name(),
                    e.getTemporalPosition().position(),
                    e.getTemporalPosition().label(),
                    e.getTemporalPosition().calendarSystem(),
                    e.getDisplayOrder()
            );
        }
    }

    public static CharacterView of(Character c, List<TimelineEvent> events) {
        return new CharacterView(
                c.getId().toString(), c.getTimelineId().toString(),
                c.getLinkedTimelineId() != null ? c.getLinkedTimelineId().toString() : null,
                c.getName(), c.getDescription(),
                c.getStartPosition(), c.getEndPosition(),
                events.stream().map(CharacterEventView::from).toList()
        );
    }
}
