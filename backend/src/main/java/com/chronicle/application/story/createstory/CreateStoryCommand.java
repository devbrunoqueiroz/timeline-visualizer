package com.chronicle.application.story.createstory;

import java.util.Objects;

public record CreateStoryCommand(String title, String description) {

    public CreateStoryCommand {
        Objects.requireNonNull(title, "Title is required");
    }
}
