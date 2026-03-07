package com.chronicle.domain.connection;

import com.chronicle.domain.timeline.TimelineEventId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConnectionRepository {
    void save(TimelineConnection connection);
    Optional<TimelineConnection> findById(ConnectionId id);
    List<TimelineConnection> findByEventId(TimelineEventId eventId);
    List<TimelineConnection> findByEventIds(Set<String> eventIds);
    void delete(ConnectionId id);
}
