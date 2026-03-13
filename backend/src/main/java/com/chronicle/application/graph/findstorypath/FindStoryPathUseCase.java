package com.chronicle.application.graph.findstorypath;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionRepository;
import com.chronicle.domain.connection.TimelineConnection;
import com.chronicle.domain.graph.GraphTraversalService;
import com.chronicle.domain.graph.StoryEdge;
import com.chronicle.domain.graph.StoryGraph;
import com.chronicle.domain.graph.StoryNode;
import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineFilter;
import com.chronicle.domain.timeline.TimelineRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds the shortest story path between two events in the narrative graph.
 *
 * The use case assembles a {@link StoryGraph} from all timelines and their connections,
 * then delegates path finding to the {@link GraphTraversalService}.
 *
 * This is additive — it does not change existing graph or timeline use cases.
 */
public class FindStoryPathUseCase implements UseCase<FindStoryPathQuery, StoryPathView> {

    private final TimelineRepository timelineRepository;
    private final ConnectionRepository connectionRepository;
    private final GraphTraversalService traversalService;

    public FindStoryPathUseCase(TimelineRepository timelineRepository,
                                 ConnectionRepository connectionRepository,
                                 GraphTraversalService traversalService) {
        this.timelineRepository = timelineRepository;
        this.connectionRepository = connectionRepository;
        this.traversalService = traversalService;
    }

    @Override
    public StoryPathView execute(FindStoryPathQuery query) {
        List<Timeline> timelines = timelineRepository.findAll(TimelineFilter.noFilter());

        List<StoryNode> storyNodes = timelines.stream()
                .flatMap(t -> t.getEvents().stream().map(e -> StoryNode.from(e, t.getId())))
                .collect(Collectors.toList());

        Set<String> nodeIds = storyNodes.stream().map(StoryNode::id).collect(Collectors.toSet());
        List<TimelineConnection> connections = connectionRepository.findByEventIds(nodeIds);
        List<StoryEdge> explicitEdges = connections.stream()
                .map(StoryEdge::from)
                .collect(Collectors.toList());

        List<StoryEdge> allEdges = new ArrayList<>(explicitEdges);
        if (!query.explicitOnly()) {
            allEdges.addAll(buildInferredEdges(timelines));
        }

        StoryGraph graph = StoryGraph.of(storyNodes, allEdges);

        var result = query.explicitOnly()
                ? traversalService.findExplicitPath(graph, query.fromEventId(), query.toEventId())
                : traversalService.findShortestPath(graph, query.fromEventId(), query.toEventId());

        return result.map(StoryPathView::from).orElse(StoryPathView.notFound());
    }

    private List<StoryEdge> buildInferredEdges(List<Timeline> timelines) {
        List<StoryEdge> result = new ArrayList<>();
        for (Timeline timeline : timelines) {
            var sorted = timeline.getEvents().stream()
                    .sorted(Comparator.comparing(e -> e.getTemporalPosition().position()))
                    .collect(Collectors.toList());
            for (int i = 0; i < sorted.size() - 1; i++) {
                String sourceId = sorted.get(i).getId().value().toString();
                String targetId = sorted.get(i + 1).getId().value().toString();
                result.add(StoryEdge.inferred(sourceId, targetId));
            }
        }
        return result;
    }
}
