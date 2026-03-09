package com.chronicle.application.timeline.createtimeline;

import com.chronicle.application.shared.DomainEventPublisher;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineRepository;
import com.chronicle.domain.timeline.TimelineVisibility;

public class CreateTimelineUseCase implements UseCase<CreateTimelineCommand, CreateTimelineResult> {

    private final TimelineRepository timelineRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateTimelineUseCase(TimelineRepository timelineRepository, DomainEventPublisher eventPublisher) {
        this.timelineRepository = timelineRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateTimelineResult execute(CreateTimelineCommand command) {
        var visibility = command.visibility() != null ? command.visibility() : TimelineVisibility.PRIVATE;
        var timeline = Timeline.create(command.name(), command.description(), visibility, command.ownerId());
        timelineRepository.save(timeline);
        eventPublisher.publishAll(timeline.pullDomainEvents());
        return CreateTimelineResult.from(timeline);
    }
}
