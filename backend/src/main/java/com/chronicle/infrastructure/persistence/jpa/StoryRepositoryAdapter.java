package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.story.Story;
import com.chronicle.domain.story.StoryId;
import com.chronicle.domain.story.StoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StoryRepositoryAdapter implements StoryRepository {

    private final StoryJpaRepository jpaRepository;
    private final StoryMapper mapper;

    public StoryRepositoryAdapter(StoryJpaRepository jpaRepository, StoryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Story story) {
        var entity = mapper.toEntity(story);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Story> findById(StoryId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Story> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(StoryId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(StoryId id) {
        return jpaRepository.existsById(id.value());
    }
}
