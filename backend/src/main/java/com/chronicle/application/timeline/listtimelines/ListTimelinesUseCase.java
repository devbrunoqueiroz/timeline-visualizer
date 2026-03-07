package com.chronicle.application.timeline.listtimelines;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.timeline.TimelineRepository;

import java.util.List;

public class ListTimelinesUseCase implements UseCase<ListTimelinesQuery, List<TimelineSummaryView>> {

    private final TimelineRepository timelineRepository;

    public ListTimelinesUseCase(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Override
    public List<TimelineSummaryView> execute(ListTimelinesQuery query) {
        return timelineRepository.findAll(query.filter()).stream()
                .map(TimelineSummaryView::from)
                .toList();
    }
}
