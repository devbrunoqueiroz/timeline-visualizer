package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.story.SessionId;
import com.chronicle.domain.story.StoryId;
import com.chronicle.domain.story.StorySession;
import com.chronicle.domain.story.StorySessionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StorySessionRepositoryAdapter implements StorySessionRepository {

    private final StorySessionJpaRepository jpaRepository;
    private final StoryMapper mapper;

    public StorySessionRepositoryAdapter(StorySessionJpaRepository jpaRepository, StoryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(StorySession session) {
        var entity = mapper.toSessionEntity(session);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<StorySession> findById(SessionId id) {
        return jpaRepository.findById(id.value()).map(mapper::toSessionDomain);
    }

    @Override
    public List<StorySession> findByStoryId(StoryId storyId) {
        return jpaRepository.findByStoryId(storyId.value()).stream()
                .map(mapper::toSessionDomain)
                .toList();
    }

    @Override
    public void delete(SessionId id) {
        jpaRepository.deleteById(id.value());
    }
}
