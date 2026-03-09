package com.chronicle.application.character.update;

import com.chronicle.application.character.get.CharacterView;
import com.chronicle.domain.character.CharacterId;
import com.chronicle.domain.character.CharacterNotFoundException;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.timeline.TimelineRepository;
import com.chronicle.domain.timeline.TimelineVisibility;

import java.util.List;

public class UpdateCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    public UpdateCharacterUseCase(CharacterRepository characterRepository, TimelineRepository timelineRepository) {
        this.characterRepository = characterRepository;
        this.timelineRepository = timelineRepository;
    }

    public CharacterView execute(UpdateCharacterCommand command) {
        var character = characterRepository.findById(CharacterId.of(command.characterId()))
                .orElseThrow(() -> new CharacterNotFoundException(command.characterId()));
        character.update(command.name(), command.description(), command.startPosition(), command.endPosition());
        characterRepository.save(character);
        // Keep linked timeline name in sync with character name
        if (character.getLinkedTimelineId() != null) {
            timelineRepository.findById(character.getLinkedTimelineId()).ifPresent(t -> {
                t.update(command.name(), command.description(), TimelineVisibility.PRIVATE);
                timelineRepository.save(t);
            });
        }
        var events = character.getLinkedTimelineId() != null
                ? timelineRepository.findById(character.getLinkedTimelineId())
                        .map(t -> (List) t.getEvents()).orElse(List.of())
                : List.of();
        return CharacterView.of(character, events);
    }
}
