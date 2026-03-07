package com.chronicle.application.timeline.deletetimeline;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.timeline.TimelineRepository;

public class DeleteTimelineUseCase implements UseCase<DeleteTimelineCommand, Void> {

    private final TimelineRepository timelineRepository;

    public DeleteTimelineUseCase(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Void execute(DeleteTimelineCommand command) {
        var id = TimelineId.of(command.timelineId());
        if (!timelineRepository.existsById(id)) {
            throw new TimelineNotFoundException(id);
        }
        timelineRepository.delete(id);
        return null;
    }
}
