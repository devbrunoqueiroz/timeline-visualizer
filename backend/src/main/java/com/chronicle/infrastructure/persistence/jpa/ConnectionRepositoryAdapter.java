package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.connection.*;
import com.chronicle.domain.timeline.TimelineEventId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ConnectionRepositoryAdapter implements ConnectionRepository {

    private final ConnectionJpaRepository jpaRepository;

    public ConnectionRepositoryAdapter(ConnectionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(TimelineConnection connection) {
        var entity = new TimelineConnectionEntity();
        entity.setId(connection.getId().value());
        entity.setSourceEventId(connection.getSourceEventId().value());
        entity.setTargetEventId(connection.getTargetEventId().value());
        entity.setDescription(connection.getDescription());
        entity.setConnectionType(connection.getConnectionType().name());
        entity.setCreatedAt(connection.getCreatedAt());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<TimelineConnection> findById(ConnectionId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<TimelineConnection> findByEventId(TimelineEventId eventId) {
        return jpaRepository.findByEventId(eventId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<TimelineConnection> findByEventIds(Set<String> eventIds) {
        var uuids = eventIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        return jpaRepository.findByEventIds(uuids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(ConnectionId id) {
        jpaRepository.deleteById(id.value());
    }

    private TimelineConnection toDomain(TimelineConnectionEntity entity) {
        return TimelineConnection.reconstitute(
                new ConnectionId(entity.getId()),
                new TimelineEventId(entity.getSourceEventId()),
                new TimelineEventId(entity.getTargetEventId()),
                entity.getDescription(),
                ConnectionType.valueOf(entity.getConnectionType())
        );
    }
}
