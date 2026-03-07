package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.connection.createconnection.CreateConnectionResult;
import com.chronicle.domain.connection.ConnectionType;

public record ConnectionResponse(String id, String sourceEventId, String targetEventId,
                                  String description, ConnectionType connectionType) {

    public static ConnectionResponse from(CreateConnectionResult result) {
        return new ConnectionResponse(
                result.id(), result.sourceEventId(), result.targetEventId(),
                result.description(), result.connectionType()
        );
    }
}
