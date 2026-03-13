package com.chronicle.domain.graph;

import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineEvent;
import com.chronicle.domain.timeline.TimelineId;

/**
 * Lightweight, immutable domain value object representing a node in the story graph.
 * Abstracts over the underlying event source so graph algorithms are source-agnostic.
 */
public record StoryNode(
        String id,
        String title,
        TemporalPosition temporalPosition,
        TimelineId timelineId) {

    public static StoryNode from(TimelineEvent event, TimelineId timelineId) {
        return new StoryNode(
                event.getId().value().toString(),
                event.getTitle(),
                event.getTemporalPosition(),
                timelineId
        );
    }
}
