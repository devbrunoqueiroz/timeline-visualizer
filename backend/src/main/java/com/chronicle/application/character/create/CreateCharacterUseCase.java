package com.chronicle.application.character.create;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.character.Character;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;
import com.chronicle.domain.timeline.TimelineVisibility;

public class CreateCharacterUseCase implements UseCase<CreateCharacterCommand, CreateCharacterResult> {

    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    public CreateCharacterUseCase(CharacterRepository characterRepository, TimelineRepository timelineRepository) {
        this.characterRepository = characterRepository;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public CreateCharacterResult execute(CreateCharacterCommand command) {
        var timelineId = TimelineId.of(command.timelineId());
        if (!timelineRepository.existsById(timelineId)) {
            throw new TimelineNotFoundException(timelineId);
        }

        // Create a dedicated sub-timeline for this character's events
        var linkedTimeline = Timeline.create(command.name(), command.description(),
                TimelineVisibility.PRIVATE, null);
        timelineRepository.save(linkedTimeline);

        var character = Character.create(
                command.name(), command.description(), timelineId,
                command.startPosition(), command.endPosition(),
                linkedTimeline.getId()
        );
        characterRepository.save(character);
        return CreateCharacterResult.from(character);
    }
}
