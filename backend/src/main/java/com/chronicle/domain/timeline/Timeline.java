package com.chronicle.domain.timeline;

import com.chronicle.domain.shared.AggregateRoot;
import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.timeline.events.EventAddedToTimeline;
import com.chronicle.domain.timeline.events.TimelineCreated;
import com.chronicle.domain.user.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timeline extends AggregateRoot<TimelineId> {

    private final TimelineId id;
    private String name;
    private String description;
    private TimelineVisibility visibility;
    private final UserId ownerId;
    private final List<TimelineEvent> events;
    private final Instant createdAt;
    private Instant updatedAt;

    private Timeline(TimelineId id, String name, String description, TimelineVisibility visibility, UserId ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.ownerId = ownerId;
        this.events = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Timeline create(String name, String description, TimelineVisibility visibility, UserId ownerId) {
        validateName(name);
        var timeline = new Timeline(TimelineId.generate(), name, description, visibility, ownerId);
        timeline.registerEvent(new TimelineCreated(timeline.id, name));
        return timeline;
    }

    public static Timeline reconstitute(TimelineId id, String name, String description,
                                        TimelineVisibility visibility, UserId ownerId,
                                        List<TimelineEvent> events, Instant createdAt, Instant updatedAt) {
        return new ReconstitutedBuilder(id, name, description, visibility, ownerId, events, createdAt, updatedAt).build();
    }

    private static class ReconstitutedBuilder {
        private final TimelineId id;
        private final String name;
        private final String description;
        private final TimelineVisibility visibility;
        private final UserId ownerId;
        private final List<TimelineEvent> events;
        private final Instant createdAt;
        private final Instant updatedAt;

        ReconstitutedBuilder(TimelineId id, String name, String description, TimelineVisibility visibility,
                             UserId ownerId, List<TimelineEvent> events, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.visibility = visibility;
            this.ownerId = ownerId;
            this.events = events;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        Timeline build() {
            var t = new Timeline(id, name, description, visibility, ownerId);
            t.events.addAll(events);
            t.updatedAt = updatedAt;
            return t;
        }
    }

    public TimelineEvent addEvent(String title, EventContent content, TemporalPosition temporalPosition) {
        int order = events.size();
        var event = TimelineEvent.create(title, content, temporalPosition, order);
        this.events.add(event);
        this.updatedAt = Instant.now();
        registerEvent(new EventAddedToTimeline(this.id, event.getId()));
        return event;
    }

    public TimelineEvent updateEvent(TimelineEventId eventId, String title, EventContent content,
                                     TemporalPosition temporalPosition) {
        var event = findEvent(eventId);
        event.update(title, content, temporalPosition);
        this.updatedAt = Instant.now();
        return event;
    }

    public void removeEvent(TimelineEventId eventId) {
        var event = findEvent(eventId);
        events.remove(event);
        reorderEvents();
        this.updatedAt = Instant.now();
    }

    public void update(String name, String description, TimelineVisibility visibility) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.updatedAt = Instant.now();
    }

    private TimelineEvent findEvent(TimelineEventId eventId) {
        return events.stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Event not found: " + eventId));
    }

    private void reorderEvents() {
        for (int i = 0; i < events.size(); i++) {
            events.get(i).setDisplayOrder(i);
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Timeline name cannot be blank");
        }
        if (name.length() > 200) {
            throw new DomainException("Timeline name cannot exceed 200 characters");
        }
    }

    public boolean isOwnedBy(UserId userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    @Override
    public TimelineId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TimelineVisibility getVisibility() { return visibility; }
    public UserId getOwnerId() { return ownerId; }
    public List<TimelineEvent> getEvents() { return Collections.unmodifiableList(events); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
