package com.chronicle.domain.story;

import java.util.List;
import java.util.Optional;

public interface StorySessionRepository {
    void save(StorySession session);
    Optional<StorySession> findById(SessionId id);
    List<StorySession> findByStoryId(StoryId storyId);
    void delete(SessionId id);
}
