package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.connection.createconnection.CreateConnectionResult;
import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.narrative.NarrativeValidationResult;

import java.util.List;

public record ConnectionResponse(String id, String sourceEventId, String targetEventId,
                                  String description, ConnectionType connectionType,
                                  List<NarrativeValidationResult> validations) {

    public static ConnectionResponse from(CreateConnectionResult result) {
        return new ConnectionResponse(
                result.id(), result.sourceEventId(), result.targetEventId(),
                result.description(), result.connectionType(),
                result.validations() != null ? result.validations() : List.of()
        );
    }
}
