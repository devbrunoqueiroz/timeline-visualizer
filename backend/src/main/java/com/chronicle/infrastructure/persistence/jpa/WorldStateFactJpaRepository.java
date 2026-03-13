package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorldStateFactJpaRepository extends JpaRepository<WorldStateFactEntity, UUID> {
    List<WorldStateFactEntity> findBySessionId(UUID sessionId);
    void deleteBySessionId(UUID sessionId);
}
