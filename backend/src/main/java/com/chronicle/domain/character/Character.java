package com.chronicle.domain.character;

import com.chronicle.domain.shared.AggregateRoot;
import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.timeline.TimelineId;

import java.math.BigDecimal;
import java.time.Instant;

public class Character extends AggregateRoot<CharacterId> {

    private final CharacterId id;
    private String name;
    private String description;
    private final TimelineId timelineId;
    private final TimelineId linkedTimelineId;
    private BigDecimal startPosition;
    private BigDecimal endPosition;
    private final Instant createdAt;

    private Character(CharacterId id, String name, String description, TimelineId timelineId,
                      TimelineId linkedTimelineId, BigDecimal startPosition, BigDecimal endPosition,
                      Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.timelineId = timelineId;
        this.linkedTimelineId = linkedTimelineId;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.createdAt = createdAt;
    }

    public static Character create(String name, String description, TimelineId timelineId,
                                   BigDecimal startPosition, BigDecimal endPosition,
                                   TimelineId linkedTimelineId) {
        validateName(name);
        return new Character(CharacterId.generate(), name, description, timelineId,
                linkedTimelineId, startPosition, endPosition, Instant.now());
    }

    public static Character reconstitute(CharacterId id, String name, String description,
                                          TimelineId timelineId, TimelineId linkedTimelineId,
                                          BigDecimal startPosition, BigDecimal endPosition,
                                          Instant createdAt) {
        return new Character(id, name, description, timelineId, linkedTimelineId,
                startPosition, endPosition, createdAt);
    }

    public void update(String name, String description, BigDecimal startPosition, BigDecimal endPosition) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Character name cannot be blank");
        }
        if (name.length() > 200) {
            throw new DomainException("Character name cannot exceed 200 characters");
        }
    }

    @Override
    public CharacterId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TimelineId getTimelineId() { return timelineId; }
    public TimelineId getLinkedTimelineId() { return linkedTimelineId; }
    public BigDecimal getStartPosition() { return startPosition; }
    public BigDecimal getEndPosition() { return endPosition; }
    public Instant getCreatedAt() { return createdAt; }
}
