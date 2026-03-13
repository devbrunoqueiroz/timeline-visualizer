package com.chronicle.application.story.createsession;

import com.chronicle.domain.story.StorySession;

import java.time.Instant;

public record CreateSessionResult(String id, String storyId, String name, Instant createdAt) {

    public static CreateSessionResult from(StorySession session) {
        return new CreateSessionResult(
                session.getId().toString(),
                session.getStoryId().toString(),
                session.getName(),
                session.getCreatedAt()
        );
    }
}
