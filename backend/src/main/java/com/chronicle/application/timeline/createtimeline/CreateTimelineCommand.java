package com.chronicle.application.timeline.createtimeline;

import com.chronicle.domain.timeline.TimelineVisibility;
import com.chronicle.domain.user.UserId;

import java.util.Objects;

public record CreateTimelineCommand(String name, String description, TimelineVisibility visibility, UserId ownerId) {

    public CreateTimelineCommand {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(ownerId, "OwnerId is required");
    }
}
