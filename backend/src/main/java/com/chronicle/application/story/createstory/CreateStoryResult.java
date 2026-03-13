package com.chronicle.application.story.createstory;

import com.chronicle.domain.story.Story;

import java.time.Instant;

public record CreateStoryResult(String id, String title, String description, Instant createdAt) {

    public static CreateStoryResult from(Story story) {
        return new CreateStoryResult(
                story.getId().toString(),
                story.getTitle(),
                story.getDescription(),
                story.getCreatedAt()
        );
    }
}
