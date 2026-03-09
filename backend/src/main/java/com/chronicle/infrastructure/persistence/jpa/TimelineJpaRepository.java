package com.chronicle.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TimelineJpaRepository extends JpaRepository<TimelineEntity, UUID> {

    @Query(value = "SELECT * FROM timelines WHERE " +
            "(:visibility IS NULL OR visibility = :visibility) AND " +
            "(:nameContains IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', CAST(:nameContains AS TEXT), '%'))) AND " +
            "(:ownerId IS NULL OR owner_id = CAST(:ownerId AS UUID)) AND " +
            "id NOT IN (SELECT linked_timeline_id FROM characters WHERE linked_timeline_id IS NOT NULL)",
            nativeQuery = true)
    List<TimelineEntity> findWithFilter(@Param("visibility") String visibility,
                                        @Param("nameContains") String nameContains,
                                        @Param("ownerId") String ownerId);
}
