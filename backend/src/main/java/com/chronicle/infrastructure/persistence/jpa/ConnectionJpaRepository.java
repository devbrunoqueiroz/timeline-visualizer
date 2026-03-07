package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ConnectionJpaRepository extends JpaRepository<TimelineConnectionEntity, UUID> {

    @Query("SELECT c FROM TimelineConnectionEntity c WHERE c.sourceEventId = :eventId OR c.targetEventId = :eventId")
    List<TimelineConnectionEntity> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT c FROM TimelineConnectionEntity c WHERE c.sourceEventId IN :eventIds OR c.targetEventId IN :eventIds")
    List<TimelineConnectionEntity> findByEventIds(@Param("eventIds") Set<UUID> eventIds);
}
