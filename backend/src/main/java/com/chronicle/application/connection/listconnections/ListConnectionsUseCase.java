package com.chronicle.application.connection.listconnections;

import com.chronicle.application.connection.createconnection.CreateConnectionResult;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionRepository;

import java.util.List;

public class ListConnectionsUseCase implements UseCase<ListConnectionsQuery, List<CreateConnectionResult>> {

    private final ConnectionRepository connectionRepository;

    public ListConnectionsUseCase(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public List<CreateConnectionResult> execute(ListConnectionsQuery query) {
        return connectionRepository.findByEventIds(query.eventIds()).stream()
                .map(CreateConnectionResult::from)
                .toList();
    }
}
