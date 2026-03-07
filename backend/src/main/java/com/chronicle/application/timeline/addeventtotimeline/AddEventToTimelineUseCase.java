package com.chronicle.application.timeline.addeventtotimeline;

import com.chronicle.application.shared.DomainEventPublisher;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.EventContent;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;

public class AddEventToTimelineUseCase implements UseCase<AddEventCommand, AddEventResult> {

    private final TimelineRepository timelineRepository;
    private final DomainEventPublisher eventPublisher;

    public AddEventToTimelineUseCase(TimelineRepository timelineRepository, DomainEventPublisher eventPublisher) {
        this.timelineRepository = timelineRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AddEventResult execute(AddEventCommand command) {
        var id = TimelineId.of(command.timelineId());
        var timeline = timelineRepository.findById(id)
                .orElseThrow(() -> new TimelineNotFoundException(id));
        var content = new EventContent(command.contentText(), command.contentType(), java.util.Map.of());
        var event = timeline.addEvent(command.title(), content, command.temporalPosition());
        timelineRepository.save(timeline);
        eventPublisher.publishAll(timeline.pullDomainEvents());
        return AddEventResult.from(event);
    }
}
