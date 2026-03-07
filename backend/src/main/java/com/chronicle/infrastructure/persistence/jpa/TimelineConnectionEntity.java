package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timeline_connections")
public class TimelineConnectionEntity {

    @Id
    private UUID id;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Column(name = "target_event_id", nullable = false)
    private UUID targetEventId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String connectionType;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(UUID sourceEventId) { this.sourceEventId = sourceEventId; }
    public UUID getTargetEventId() { return targetEventId; }
    public void setTargetEventId(UUID targetEventId) { this.targetEventId = targetEventId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
