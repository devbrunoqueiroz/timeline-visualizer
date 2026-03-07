package com.chronicle.application.timeline.updateevent;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.EventContent;
import com.chronicle.domain.timeline.TimelineEventId;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;

public class UpdateEventUseCase implements UseCase<UpdateEventCommand, UpdateEventResult> {

    private final TimelineRepository timelineRepository;

    public UpdateEventUseCase(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Override
    public UpdateEventResult execute(UpdateEventCommand command) {
        var timelineId = TimelineId.of(command.timelineId());
        var timeline = timelineRepository.findById(timelineId)
                .orElseThrow(() -> new TimelineNotFoundException(timelineId));
        var eventId = TimelineEventId.of(command.eventId());
        var content = new EventContent(command.contentText(), command.contentType(), java.util.Map.of());
        var event = timeline.updateEvent(eventId, command.title(), content, command.temporalPosition());
        timelineRepository.save(timeline);
        return UpdateEventResult.from(event);
    }
}
