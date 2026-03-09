package com.chronicle.application.character.list;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.character.CharacterRepository;

import java.util.List;

public class ListCharactersUseCase implements UseCase<ListCharactersQuery, List<CharacterSummaryView>> {

    private final CharacterRepository characterRepository;

    public ListCharactersUseCase(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Override
    public List<CharacterSummaryView> execute(ListCharactersQuery query) {
        return characterRepository.findByTimelineId(query.timelineId()).stream()
                .map(CharacterSummaryView::from)
                .toList();
    }
}
