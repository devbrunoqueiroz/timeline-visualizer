package com.chronicle.application.timeline.gettimeline;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;

public class GetTimelineUseCase implements UseCase<GetTimelineQuery, TimelineView> {

    private final TimelineRepository timelineRepository;

    public GetTimelineUseCase(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Override
    public TimelineView execute(GetTimelineQuery query) {
        var timeline = timelineRepository.findById(query.timelineId())
                .orElseThrow(() -> new TimelineNotFoundException(query.timelineId()));
        return TimelineView.from(timeline);
    }
}
