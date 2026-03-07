package com.chronicle.application.graph.geteventgraph;

import com.chronicle.application.narrative.NarrativeValidationService;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionRepository;
import com.chronicle.domain.connection.TimelineConnection;
import com.chronicle.domain.narrative.NarrativeValidationResult;
import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineEvent;
import com.chronicle.domain.timeline.TimelineFilter;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.timeline.TimelineRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GetEventGraphUseCase implements UseCase<GetEventGraphQuery, EventGraphView> {

    private final TimelineRepository timelineRepository;
    private final ConnectionRepository connectionRepository;
    private final NarrativeValidationService validationService;

    public GetEventGraphUseCase(TimelineRepository timelineRepository,
                                 ConnectionRepository connectionRepository,
                                 NarrativeValidationService validationService) {
        this.timelineRepository = timelineRepository;
        this.connectionRepository = connectionRepository;
        this.validationService = validationService;
    }

    @Override
    public EventGraphView execute(GetEventGraphQuery query) {
        List<Timeline> timelines = loadTimelines(query);

        // Map eventId → event, for validation lookups
        Map<String, TimelineEvent> eventById = new HashMap<>();
        for (Timeline t : timelines) {
            for (TimelineEvent e : t.getEvents()) {
                eventById.put(e.getId().value().toString(), e);
            }
        }

        // Nodes
        List<GraphNodeView> nodes = timelines.stream()
                .flatMap(t -> t.getEvents().stream()
                        .map(e -> GraphNodeView.from(e, t.getId().value().toString())))
                .collect(Collectors.toList());

        Set<String> eventIds = nodes.stream().map(GraphNodeView::id).collect(Collectors.toSet());

        // Explicit edges + validations
        List<TimelineConnection> connections = connectionRepository.findByEventIds(eventIds);
        List<GraphEdgeView> explicitEdges = connections.stream()
                .map(GraphEdgeView::from)
                .collect(Collectors.toList());

        List<NarrativeValidationResult> validations = connections.stream()
                .flatMap(c -> validateConnection(c, eventById).stream())
                .collect(Collectors.toList());

        // Inferred temporal edges (visual timeline backbone)
        List<GraphEdgeView> inferredEdges = buildInferredEdges(timelines);

        List<GraphEdgeView> allEdges = new ArrayList<>(explicitEdges);
        allEdges.addAll(inferredEdges);

        return new EventGraphView(nodes, allEdges, validations);
    }

    private List<Timeline> loadTimelines(GetEventGraphQuery query) {
        if (query.timelineIds() == null || query.timelineIds().isEmpty()) {
            return timelineRepository.findAll(TimelineFilter.noFilter());
        }
        return query.timelineIds().stream()
                .map(id -> timelineRepository.findById(TimelineId.of(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * For each timeline, sort events by temporalPosition and connect consecutive pairs
     * with a virtual inferred edge. These represent the implicit temporal backbone.
     */
    private List<GraphEdgeView> buildInferredEdges(List<Timeline> timelines) {
        List<GraphEdgeView> result = new ArrayList<>();
        for (Timeline timeline : timelines) {
            List<TimelineEvent> sorted = timeline.getEvents().stream()
                    .sorted(Comparator.comparing(e -> e.getTemporalPosition().position()))
                    .collect(Collectors.toList());
            for (int i = 0; i < sorted.size() - 1; i++) {
                String sourceId = sorted.get(i).getId().value().toString();
                String targetId = sorted.get(i + 1).getId().value().toString();
                result.add(GraphEdgeView.inferred(sourceId, targetId));
            }
        }
        return result;
    }

    private List<NarrativeValidationResult> validateConnection(TimelineConnection connection,
                                                                Map<String, TimelineEvent> eventById) {
        var source = eventById.get(connection.getSourceEventId().value().toString());
        var target = eventById.get(connection.getTargetEventId().value().toString());
        if (source == null || target == null) return List.of();
        return validationService.validate(
                connection.getId().value().toString(),
                connection.getConnectionType(),
                source.getTemporalPosition(),
                target.getTemporalPosition()
        );
    }
}
