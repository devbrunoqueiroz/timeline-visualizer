package com.chronicle.application.connection.deleteconnection;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionId;
import com.chronicle.domain.connection.ConnectionNotFoundException;
import com.chronicle.domain.connection.ConnectionRepository;

public class DeleteConnectionUseCase implements UseCase<DeleteConnectionCommand, Void> {

    private final ConnectionRepository connectionRepository;

    public DeleteConnectionUseCase(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public Void execute(DeleteConnectionCommand command) {
        var id = ConnectionId.of(command.connectionId());
        if (connectionRepository.findById(id).isEmpty()) {
            throw new ConnectionNotFoundException(id);
        }
        connectionRepository.delete(id);
        return null;
    }
}
