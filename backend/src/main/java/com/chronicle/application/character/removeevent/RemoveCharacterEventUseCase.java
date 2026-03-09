package com.chronicle.application.character.removeevent;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.character.CharacterId;
import com.chronicle.domain.character.CharacterNotFoundException;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.timeline.TimelineEventId;
import com.chronicle.domain.timeline.TimelineRepository;

public class RemoveCharacterEventUseCase implements UseCase<RemoveCharacterEventCommand, Void> {

    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    public RemoveCharacterEventUseCase(CharacterRepository characterRepository, TimelineRepository timelineRepository) {
        this.characterRepository = characterRepository;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Void execute(RemoveCharacterEventCommand command) {
        var character = characterRepository.findById(CharacterId.of(command.characterId()))
                .orElseThrow(() -> new CharacterNotFoundException(command.characterId()));
        if (character.getLinkedTimelineId() == null) {
            throw new DomainException("Character has no linked timeline");
        }
        var timeline = timelineRepository.findById(character.getLinkedTimelineId())
                .orElseThrow(() -> new DomainException("Linked timeline not found"));
        timeline.removeEvent(TimelineEventId.of(command.eventId()));
        timelineRepository.save(timeline);
        return null;
    }
}
