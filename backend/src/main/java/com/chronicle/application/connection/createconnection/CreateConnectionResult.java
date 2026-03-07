package com.chronicle.application.connection.createconnection;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;

public record CreateConnectionResult(String id, String sourceEventId, String targetEventId,
                                      String description, ConnectionType connectionType) {

    public static CreateConnectionResult from(TimelineConnection connection) {
        return new CreateConnectionResult(
                connection.getId().value().toString(),
                connection.getSourceEventId().value().toString(),
                connection.getTargetEventId().value().toString(),
                connection.getDescription(),
                connection.getConnectionType()
        );
    }
}
