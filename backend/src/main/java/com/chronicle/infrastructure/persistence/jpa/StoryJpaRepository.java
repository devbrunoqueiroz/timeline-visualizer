package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoryJpaRepository extends JpaRepository<StoryEntity, UUID> {
}
