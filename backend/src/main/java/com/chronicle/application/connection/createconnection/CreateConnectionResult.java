package com.chronicle.application.connection.createconnection;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;
import com.chronicle.domain.narrative.NarrativeValidationResult;

import java.util.List;

public record CreateConnectionResult(String id, String sourceEventId, String targetEventId,
                                      String description, ConnectionType connectionType,
                                      List<NarrativeValidationResult> validations) {

    public static CreateConnectionResult from(TimelineConnection connection) {
        return new CreateConnectionResult(
                connection.getId().value().toString(),
                connection.getSourceEventId().value().toString(),
                connection.getTargetEventId().value().toString(),
                connection.getDescription(),
                connection.getConnectionType(),
                List.of()
        );
    }

    public static CreateConnectionResult from(TimelineConnection connection,
                                               List<NarrativeValidationResult> validations) {
        return new CreateConnectionResult(
                connection.getId().value().toString(),
                connection.getSourceEventId().value().toString(),
                connection.getTargetEventId().value().toString(),
                connection.getDescription(),
                connection.getConnectionType(),
                validations
        );
    }
}
