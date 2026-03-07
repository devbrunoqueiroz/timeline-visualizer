package com.chronicle.application.timeline.removeevent;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.TimelineEventId;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;

public class RemoveEventUseCase implements UseCase<RemoveEventCommand, Void> {

    private final TimelineRepository timelineRepository;

    public RemoveEventUseCase(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Void execute(RemoveEventCommand command) {
        var timelineId = TimelineId.of(command.timelineId());
        var timeline = timelineRepository.findById(timelineId)
                .orElseThrow(() -> new TimelineNotFoundException(timelineId));
        timeline.removeEvent(TimelineEventId.of(command.eventId()));
        timelineRepository.save(timeline);
        return null;
    }
}
