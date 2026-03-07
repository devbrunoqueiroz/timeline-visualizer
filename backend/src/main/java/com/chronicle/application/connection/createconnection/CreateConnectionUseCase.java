package com.chronicle.application.connection.createconnection;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionRepository;
import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;
import com.chronicle.domain.timeline.TimelineEventId;

public class CreateConnectionUseCase implements UseCase<CreateConnectionCommand, CreateConnectionResult> {

    private final ConnectionRepository connectionRepository;

    public CreateConnectionUseCase(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public CreateConnectionResult execute(CreateConnectionCommand command) {
        var sourceId = TimelineEventId.of(command.sourceEventId());
        var targetId = TimelineEventId.of(command.targetEventId());
        var type = command.connectionType() != null ? command.connectionType() : ConnectionType.REFERENCE;
        var connection = TimelineConnection.create(sourceId, targetId, command.description(), type);
        connectionRepository.save(connection);
        return CreateConnectionResult.from(connection);
    }
}
