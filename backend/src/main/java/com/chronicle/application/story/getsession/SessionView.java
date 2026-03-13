package com.chronicle.application.story.getsession;

import com.chronicle.domain.story.StorySession;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SessionView(
        String id,
        String storyId,
        String name,
        Map<String, String> worldStateFacts,
        List<String> appliedSceneIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static SessionView from(StorySession session) {
        return new SessionView(
                session.getId().toString(),
                session.getStoryId().toString(),
                session.getName(),
                session.getWorldState().getFacts(),
                session.getAppliedSceneIds().stream()
                        .map(id -> id.toString())
                        .toList(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
