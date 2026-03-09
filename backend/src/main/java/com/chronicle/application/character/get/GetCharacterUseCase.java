package com.chronicle.application.character.get;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.character.CharacterNotFoundException;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.domain.timeline.TimelineEvent;
import com.chronicle.domain.timeline.TimelineRepository;

import java.util.List;

public class GetCharacterUseCase implements UseCase<GetCharacterQuery, CharacterView> {

    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    public GetCharacterUseCase(CharacterRepository characterRepository, TimelineRepository timelineRepository) {
        this.characterRepository = characterRepository;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public CharacterView execute(GetCharacterQuery query) {
        var character = characterRepository.findById(query.characterId())
                .orElseThrow(() -> new CharacterNotFoundException(query.characterId().toString()));

        List<TimelineEvent> events = List.of();
        if (character.getLinkedTimelineId() != null) {
            events = timelineRepository.findById(character.getLinkedTimelineId())
                    .map(t -> t.getEvents())
                    .orElse(List.of());
        }

        return CharacterView.of(character, events);
    }
}
