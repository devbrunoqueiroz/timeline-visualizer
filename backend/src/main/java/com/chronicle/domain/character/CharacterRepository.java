package com.chronicle.domain.character;

import com.chronicle.domain.timeline.TimelineId;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository {
    void save(Character character);
    Optional<Character> findById(CharacterId id);
    List<Character> findByTimelineId(TimelineId timelineId);
    void delete(CharacterId id);
    boolean existsById(CharacterId id);
}
