package com.chronicle.application.graph.geteventgraph;

import com.chronicle.domain.timeline.TimelineEvent;

import java.math.BigDecimal;

public record GraphNodeView(String id, String title, String contentText, String contentType,
                             String temporalLabel, BigDecimal temporalPosition, String calendarSystem,
                             String timelineId, String timelineName, int displayOrder) {

    public static GraphNodeView from(TimelineEvent event, String timelineId, String timelineName) {
        return new GraphNodeView(
                event.getId().value().toString(),
                event.getTitle(),
                event.getContent().text(),
                event.getContent().type().name(),
                event.getTemporalPosition().label(),
                event.getTemporalPosition().position(),
                event.getTemporalPosition().calendarSystem(),
                timelineId,
                timelineName,
                event.getDisplayOrder()
        );
    }
}
