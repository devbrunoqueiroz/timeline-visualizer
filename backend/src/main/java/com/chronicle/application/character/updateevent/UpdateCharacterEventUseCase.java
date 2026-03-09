package com.chronicle.application.character.updateevent;

import com.chronicle.application.character.addevent.CharacterEventResult;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.character.CharacterId;
import com.chronicle.domain.character.CharacterNotFoundException;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.timeline.EventContent;
import com.chronicle.domain.timeline.TimelineEventId;
import com.chronicle.domain.timeline.TimelineRepository;

public class UpdateCharacterEventUseCase implements UseCase<UpdateCharacterEventCommand, CharacterEventResult> {

    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    public UpdateCharacterEventUseCase(CharacterRepository characterRepository, TimelineRepository timelineRepository) {
        this.characterRepository = characterRepository;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public CharacterEventResult execute(UpdateCharacterEventCommand command) {
        var character = characterRepository.findById(CharacterId.of(command.characterId()))
                .orElseThrow(() -> new CharacterNotFoundException(command.characterId()));
        if (character.getLinkedTimelineId() == null) {
            throw new DomainException("Character has no linked timeline");
        }
        var timeline = timelineRepository.findById(character.getLinkedTimelineId())
                .orElseThrow(() -> new DomainException("Linked timeline not found"));
        var content = new EventContent(command.contentText(), command.contentType(), java.util.Map.of());
        var event = timeline.updateEvent(TimelineEventId.of(command.eventId()), command.title(),
                content, command.temporalPosition());
        timelineRepository.save(timeline);
        return CharacterEventResult.from(event);
    }
}
