package com.chronicle.application.timeline.updatetimeline;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;
import com.chronicle.domain.timeline.TimelineVisibility;

public class UpdateTimelineUseCase implements UseCase<UpdateTimelineCommand, UpdateTimelineResult> {

    private final TimelineRepository timelineRepository;

    public UpdateTimelineUseCase(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Override
    public UpdateTimelineResult execute(UpdateTimelineCommand command) {
        var id = TimelineId.of(command.timelineId());
        var timeline = timelineRepository.findById(id)
                .orElseThrow(() -> new TimelineNotFoundException(id));
        var visibility = command.visibility() != null ? command.visibility() : TimelineVisibility.PRIVATE;
        timeline.update(command.name(), command.description(), visibility);
        timelineRepository.save(timeline);
        return UpdateTimelineResult.from(timeline);
    }
}
