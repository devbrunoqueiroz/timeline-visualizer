package com.chronicle.domain.graph;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;

/**
 * Immutable domain value object representing a directed edge in the story graph.
 * Edges can be explicit (user-created connections) or inferred (temporal backbone).
 */
public record StoryEdge(
        String id,
        String sourceNodeId,
        String targetNodeId,
        ConnectionType connectionType,
        boolean inferred) {

    public static StoryEdge from(TimelineConnection connection) {
        return new StoryEdge(
                connection.getId().value().toString(),
                connection.getSourceEventId().value().toString(),
                connection.getTargetEventId().value().toString(),
                connection.getConnectionType(),
                false
        );
    }

    /** Virtual edge representing implicit temporal ordering within a timeline. */
    public static StoryEdge inferred(String sourceId, String targetId) {
        return new StoryEdge(
                "inferred:" + sourceId + ":" + targetId,
                sourceId,
                targetId,
                null,
                true
        );
    }

    public boolean isExplicit() {
        return !inferred;
    }
}
