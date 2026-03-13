package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

public class StorySessionNotFoundException extends DomainException {

    public StorySessionNotFoundException(SessionId id) {
        super("StorySession not found: " + id.value());
    }
}
