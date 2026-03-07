package com.chronicle.application.graph.geteventgraph;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionRepository;
import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineFilter;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetEventGraphUseCase implements UseCase<GetEventGraphQuery, EventGraphView> {

    private final TimelineRepository timelineRepository;
    private final ConnectionRepository connectionRepository;

    public GetEventGraphUseCase(TimelineRepository timelineRepository, ConnectionRepository connectionRepository) {
        this.timelineRepository = timelineRepository;
        this.connectionRepository = connectionRepository;
    }

    @Override
    public EventGraphView execute(GetEventGraphQuery query) {
        List<Timeline> timelines;
        if (query.timelineIds() == null || query.timelineIds().isEmpty()) {
            timelines = timelineRepository.findAll(TimelineFilter.noFilter());
        } else {
            timelines = query.timelineIds().stream()
                    .map(id -> timelineRepository.findById(TimelineId.of(id)))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
        }

        var nodes = timelines.stream()
                .flatMap(t -> t.getEvents().stream()
                        .map(e -> GraphNodeView.from(e, t.getId().value().toString())))
                .collect(Collectors.toList());

        var eventIds = nodes.stream()
                .map(GraphNodeView::id)
                .collect(Collectors.toSet());

        var edges = connectionRepository.findByEventIds(eventIds).stream()
                .map(GraphEdgeView::from)
                .collect(Collectors.toList());

        return new EventGraphView(nodes, edges);
    }
}
