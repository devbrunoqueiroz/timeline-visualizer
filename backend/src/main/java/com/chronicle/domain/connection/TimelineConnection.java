package com.chronicle.domain.connection;

import com.chronicle.domain.shared.AggregateRoot;
import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.timeline.TimelineEventId;

import java.time.Instant;

public class TimelineConnection extends AggregateRoot<ConnectionId> {

    private final ConnectionId id;
    private final TimelineEventId sourceEventId;
    private final TimelineEventId targetEventId;
    private String description;
    private ConnectionType connectionType;
    private final Instant createdAt;

    private TimelineConnection(ConnectionId id, TimelineEventId sourceEventId,
                                TimelineEventId targetEventId, String description,
                                ConnectionType connectionType) {
        this.id = id;
        this.sourceEventId = sourceEventId;
        this.targetEventId = targetEventId;
        this.description = description;
        this.connectionType = connectionType;
        this.createdAt = Instant.now();
    }

    public static TimelineConnection create(TimelineEventId sourceEventId, TimelineEventId targetEventId,
                                             String description, ConnectionType connectionType) {
        if (sourceEventId.equals(targetEventId)) {
            throw new DomainException("Cannot connect an event to itself");
        }
        return new TimelineConnection(ConnectionId.generate(), sourceEventId, targetEventId,
                description, connectionType);
    }

    public static TimelineConnection reconstitute(ConnectionId id, TimelineEventId sourceEventId,
                                                   TimelineEventId targetEventId, String description,
                                                   ConnectionType connectionType) {
        return new TimelineConnection(id, sourceEventId, targetEventId, description, connectionType);
    }

    @Override
    public ConnectionId getId() { return id; }
    public TimelineEventId getSourceEventId() { return sourceEventId; }
    public TimelineEventId getTargetEventId() { return targetEventId; }
    public String getDescription() { return description; }
    public ConnectionType getConnectionType() { return connectionType; }
    public Instant getCreatedAt() { return createdAt; }
}
