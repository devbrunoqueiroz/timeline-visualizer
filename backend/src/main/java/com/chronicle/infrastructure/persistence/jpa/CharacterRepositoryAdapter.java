package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.character.Character;
import com.chronicle.domain.character.CharacterId;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.timeline.TimelineId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CharacterRepositoryAdapter implements CharacterRepository {

    private final CharacterJpaRepository jpaRepository;

    public CharacterRepositoryAdapter(CharacterJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Character character) {
        jpaRepository.save(toEntity(character));
    }

    @Override
    public Optional<Character> findById(CharacterId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Character> findByTimelineId(TimelineId timelineId) {
        return jpaRepository.findByTimelineId(timelineId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(CharacterId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(CharacterId id) {
        return jpaRepository.existsById(id.value());
    }

    private CharacterEntity toEntity(Character character) {
        var entity = new CharacterEntity();
        entity.setId(character.getId().value());
        entity.setTimelineId(character.getTimelineId().value());
        entity.setLinkedTimelineId(character.getLinkedTimelineId() != null
                ? character.getLinkedTimelineId().value() : null);
        entity.setName(character.getName());
        entity.setDescription(character.getDescription());
        entity.setStartPosition(character.getStartPosition());
        entity.setEndPosition(character.getEndPosition());
        entity.setCreatedAt(character.getCreatedAt());
        return entity;
    }

    private Character toDomain(CharacterEntity entity) {
        var linkedTimelineId = entity.getLinkedTimelineId() != null
                ? new TimelineId(entity.getLinkedTimelineId()) : null;
        return Character.reconstitute(
                new CharacterId(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                new TimelineId(entity.getTimelineId()),
                linkedTimelineId,
                entity.getStartPosition(),
                entity.getEndPosition(),
                entity.getCreatedAt()
        );
    }
}
