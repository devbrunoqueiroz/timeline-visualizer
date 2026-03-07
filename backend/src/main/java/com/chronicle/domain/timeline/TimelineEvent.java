package com.chronicle.domain.timeline;

import com.chronicle.domain.shared.DomainException;

public class TimelineEvent {

    private final TimelineEventId id;
    private String title;
    private EventContent content;
    private TemporalPosition temporalPosition;
    private int displayOrder;

    private TimelineEvent(TimelineEventId id, String title, EventContent content,
                          TemporalPosition temporalPosition, int displayOrder) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.temporalPosition = temporalPosition;
        this.displayOrder = displayOrder;
    }

    public static TimelineEvent create(String title, EventContent content,
                                       TemporalPosition temporalPosition, int displayOrder) {
        validateTitle(title);
        validateTemporalPosition(temporalPosition);
        return new TimelineEvent(TimelineEventId.generate(), title, content, temporalPosition, displayOrder);
    }

    public static TimelineEvent reconstitute(TimelineEventId id, String title, EventContent content,
                                             TemporalPosition temporalPosition, int displayOrder) {
        return new TimelineEvent(id, title, content, temporalPosition, displayOrder);
    }

    public void update(String title, EventContent content, TemporalPosition temporalPosition) {
        validateTitle(title);
        validateTemporalPosition(temporalPosition);
        this.title = title;
        this.content = content;
        this.temporalPosition = temporalPosition;
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainException("Event title cannot be blank");
        }
        if (title.length() > 300) {
            throw new DomainException("Event title cannot exceed 300 characters");
        }
    }

    private static void validateTemporalPosition(TemporalPosition temporalPosition) {
        if (temporalPosition == null) {
            throw new DomainException("Event temporal position cannot be null");
        }
    }

    public TimelineEventId getId() { return id; }
    public String getTitle() { return title; }
    public EventContent getContent() { return content; }
    public TemporalPosition getTemporalPosition() { return temporalPosition; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
