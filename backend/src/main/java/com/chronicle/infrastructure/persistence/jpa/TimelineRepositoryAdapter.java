package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.timeline.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TimelineRepositoryAdapter implements TimelineRepository {

    private final TimelineJpaRepository jpaRepository;
    private final TimelineMapper mapper;

    public TimelineRepositoryAdapter(TimelineJpaRepository jpaRepository, TimelineMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Timeline timeline) {
        var entity = mapper.toEntity(timeline);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Timeline> findById(TimelineId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Timeline> findAll(TimelineFilter filter) {
        var visibilityStr = filter.visibility() != null ? filter.visibility().name() : null;
        var ownerIdStr = filter.ownerId() != null ? filter.ownerId().toString() : null;
        return jpaRepository.findWithFilter(visibilityStr, filter.nameContains(), ownerIdStr)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(TimelineId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(TimelineId id) {
        return jpaRepository.existsById(id.value());
    }
}
