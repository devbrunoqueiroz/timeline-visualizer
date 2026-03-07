package com.chronicle.application.graph.geteventgraph;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;

public record GraphEdgeView(String id, String sourceEventId, String targetEventId,
                             String description, ConnectionType connectionType) {

    public static GraphEdgeView from(TimelineConnection connection) {
        return new GraphEdgeView(
                connection.getId().value().toString(),
                connection.getSourceEventId().value().toString(),
                connection.getTargetEventId().value().toString(),
                connection.getDescription(),
                connection.getConnectionType()
        );
    }
}
