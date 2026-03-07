package com.chronicle.application.connection.createconnection;

import com.chronicle.domain.connection.ConnectionType;

public record CreateConnectionCommand(String sourceEventId, String targetEventId,
                                       String description, ConnectionType connectionType) {
}
