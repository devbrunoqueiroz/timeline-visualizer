package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID timelineId;

    @Column
    private UUID linkedTimelineId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 20, scale = 6)
    private BigDecimal startPosition;

    @Column(precision = 20, scale = 6)
    private BigDecimal endPosition;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTimelineId() { return timelineId; }
    public void setTimelineId(UUID timelineId) { this.timelineId = timelineId; }
    public UUID getLinkedTimelineId() { return linkedTimelineId; }
    public void setLinkedTimelineId(UUID linkedTimelineId) { this.linkedTimelineId = linkedTimelineId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getStartPosition() { return startPosition; }
    public void setStartPosition(BigDecimal startPosition) { this.startPosition = startPosition; }
    public BigDecimal getEndPosition() { return endPosition; }
    public void setEndPosition(BigDecimal endPosition) { this.endPosition = endPosition; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
