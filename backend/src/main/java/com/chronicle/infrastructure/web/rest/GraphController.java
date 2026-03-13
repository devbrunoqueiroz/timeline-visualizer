package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.graph.findstorypath.FindStoryPathQuery;
import com.chronicle.application.graph.findstorypath.FindStoryPathUseCase;
import com.chronicle.application.graph.findstorypath.StoryPathView;
import com.chronicle.application.graph.geteventgraph.EventGraphView;
import com.chronicle.application.graph.geteventgraph.GetEventGraphQuery;
import com.chronicle.application.graph.geteventgraph.GetEventGraphUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/graph")
public class GraphController {

    private final GetEventGraphUseCase getEventGraph;
    private final FindStoryPathUseCase findStoryPath;

    public GraphController(GetEventGraphUseCase getEventGraph, FindStoryPathUseCase findStoryPath) {
        this.getEventGraph = getEventGraph;
        this.findStoryPath = findStoryPath;
    }

    @GetMapping
    public EventGraphView getGraph(
            @RequestParam(required = false) Set<String> timelineIds) {
        return getEventGraph.execute(new GetEventGraphQuery(timelineIds));
    }

    /**
     * Finds the shortest story path between two events in the narrative graph.
     *
     * GET /api/v1/graph/path?from={eventId}&amp;to={eventId}&amp;explicitOnly=false
     *
     * @param from         source event UUID
     * @param to           target event UUID
     * @param explicitOnly if true, only traverse user-created connections (not inferred temporal edges)
     */
    @GetMapping("/path")
    public StoryPathView findPath(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false, defaultValue = "false") boolean explicitOnly) {
        var query = new FindStoryPathQuery(from, to, explicitOnly);
        return findStoryPath.execute(query);
    }
}
