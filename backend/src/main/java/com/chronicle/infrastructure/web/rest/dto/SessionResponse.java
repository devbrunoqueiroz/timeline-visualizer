package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.story.getsession.SessionView;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SessionResponse(
        String id,
        String storyId,
        String name,
        Map<String, String> worldStateFacts,
        List<String> appliedSceneIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static SessionResponse from(SessionView view) {
        return new SessionResponse(
                view.id(),
                view.storyId(),
                view.name(),
                view.worldStateFacts(),
                view.appliedSceneIds(),
                view.createdAt(),
                view.updatedAt()
        );
    }
}
