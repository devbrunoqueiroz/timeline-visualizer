package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CharacterJpaRepository extends JpaRepository<CharacterEntity, UUID> {
    List<CharacterEntity> findByTimelineId(UUID timelineId);
}
