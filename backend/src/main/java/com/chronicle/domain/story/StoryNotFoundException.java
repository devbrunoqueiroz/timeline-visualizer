package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

public class StoryNotFoundException extends DomainException {

    public StoryNotFoundException(StoryId id) {
        super("Story not found: " + id.value());
    }
}
