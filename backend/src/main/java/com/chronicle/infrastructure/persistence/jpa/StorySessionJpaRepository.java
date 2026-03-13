package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StorySessionJpaRepository extends JpaRepository<StorySessionEntity, UUID> {
    List<StorySessionEntity> findByStoryId(UUID storyId);
}
