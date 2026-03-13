package com.chronicle.application.story.liststories;

import com.chronicle.domain.story.Story;

import java.time.Instant;

public record StorySummaryView(
        String id,
        String title,
        String description,
        int sceneCount,
        Instant createdAt
) {
    public static StorySummaryView from(Story story) {
        return new StorySummaryView(
                story.getId().toString(),
                story.getTitle(),
                story.getDescription(),
                story.getScenes().size(),
                story.getCreatedAt()
        );
    }
}
