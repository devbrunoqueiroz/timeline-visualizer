package com.chronicle.domain.character;

import com.chronicle.domain.timeline.EventContent;
import com.chronicle.domain.timeline.TemporalPosition;

public class CharacterEvent {

    private final CharacterEventId id;
    private String title;
    private EventContent content;
    private TemporalPosition temporalPosition;
    private int displayOrder;

    private CharacterEvent(CharacterEventId id, String title, EventContent content,
                           TemporalPosition temporalPosition, int displayOrder) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.temporalPosition = temporalPosition;
        this.displayOrder = displayOrder;
    }

    public static CharacterEvent create(String title, EventContent content,
                                        TemporalPosition temporalPosition, int displayOrder) {
        return new CharacterEvent(CharacterEventId.generate(), title, content, temporalPosition, displayOrder);
    }

    public static CharacterEvent reconstitute(CharacterEventId id, String title, EventContent content,
                                               TemporalPosition temporalPosition, int displayOrder) {
        return new CharacterEvent(id, title, content, temporalPosition, displayOrder);
    }

    public void update(String title, EventContent content, TemporalPosition temporalPosition) {
        this.title = title;
        this.content = content;
        this.temporalPosition = temporalPosition;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public CharacterEventId getId() { return id; }
    public String getTitle() { return title; }
    public EventContent getContent() { return content; }
    public TemporalPosition getTemporalPosition() { return temporalPosition; }
    public int getDisplayOrder() { return displayOrder; }
}
