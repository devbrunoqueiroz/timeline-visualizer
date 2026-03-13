package com.chronicle.application.story.liststories;

import com.chronicle.domain.story.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListStoriesUseCase {

    private final StoryRepository storyRepository;

    public ListStoriesUseCase(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public List<StorySummaryView> execute() {
        return storyRepository.findAll().stream()
                .map(StorySummaryView::from)
                .toList();
    }
}
