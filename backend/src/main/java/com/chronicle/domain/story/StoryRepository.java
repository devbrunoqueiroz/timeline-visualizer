package com.chronicle.domain.story;

import java.util.List;
import java.util.Optional;

public interface StoryRepository {
    void save(Story story);
    Optional<Story> findById(StoryId id);
    List<Story> findAll();
    void delete(StoryId id);
    boolean existsById(StoryId id);
}
