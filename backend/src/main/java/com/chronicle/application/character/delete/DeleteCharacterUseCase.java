package com.chronicle.application.character.delete;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.character.CharacterId;
import com.chronicle.domain.character.CharacterNotFoundException;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.timeline.TimelineRepository;

public class DeleteCharacterUseCase implements UseCase<DeleteCharacterCommand, Void> {

    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    public DeleteCharacterUseCase(CharacterRepository characterRepository, TimelineRepository timelineRepository) {
        this.characterRepository = characterRepository;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Void execute(DeleteCharacterCommand command) {
        var id = CharacterId.of(command.characterId());
        var character = characterRepository.findById(id)
                .orElseThrow(() -> new CharacterNotFoundException(command.characterId()));
        characterRepository.delete(id);
        if (character.getLinkedTimelineId() != null) {
            timelineRepository.delete(character.getLinkedTimelineId());
        }
        return null;
    }
}
