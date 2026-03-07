package com.chronicle.application.timeline.createtimeline;

import com.chronicle.domain.timeline.TimelineVisibility;

import java.util.Objects;

public record CreateTimelineCommand(String name, String description, TimelineVisibility visibility) {

    public CreateTimelineCommand {
        Objects.requireNonNull(name, "Name is required");
    }
}
