package com.chronicle.application.story.getstory;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.story.StoryNotFoundException;
import com.chronicle.domain.story.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetStoryUseCase implements UseCase<GetStoryQuery, StoryView> {

    private final StoryRepository storyRepository;

    public GetStoryUseCase(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @Override
    public StoryView execute(GetStoryQuery query) {
        var story = storyRepository.findById(query.storyId())
                .orElseThrow(() -> new StoryNotFoundException(query.storyId()));
        return StoryView.from(story);
    }
}
