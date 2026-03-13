package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SceneJpaRepository extends JpaRepository<SceneEntity, UUID> {
    List<SceneEntity> findByStoryId(UUID storyId);
}
