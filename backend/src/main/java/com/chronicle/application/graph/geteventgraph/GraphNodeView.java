package com.chronicle.application.graph.geteventgraph;

import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineEvent;

public record GraphNodeView(String id, String title, String timelineId, TemporalPosition temporalPosition,
                             int displayOrder) {

    public static GraphNodeView from(TimelineEvent event, String timelineId) {
        return new GraphNodeView(
                event.getId().value().toString(),
                event.getTitle(),
                timelineId,
                event.getTemporalPosition(),
                event.getDisplayOrder()
        );
    }
}
