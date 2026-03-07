package com.chronicle.application.graph.geteventgraph;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;

public record GraphEdgeView(String id, String sourceEventId, String targetEventId,
                             String description, ConnectionType connectionType,
                             boolean inferred) {

    public static GraphEdgeView from(TimelineConnection connection) {
        return new GraphEdgeView(
                connection.getId().value().toString(),
                connection.getSourceEventId().value().toString(),
                connection.getTargetEventId().value().toString(),
                connection.getDescription(),
                connection.getConnectionType(),
                false
        );
    }

    /** Virtual edge used to show timeline backbone in the graph — not persisted. */
    public static GraphEdgeView inferred(String sourceEventId, String targetEventId) {
        return new GraphEdgeView(
                "inferred:" + sourceEventId + ":" + targetEventId,
                sourceEventId,
                targetEventId,
                null,
                null,
                true
        );
    }
}
