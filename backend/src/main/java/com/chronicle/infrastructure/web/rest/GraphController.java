package com.chronicle.infrastructure.web.rest;

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

    public GraphController(GetEventGraphUseCase getEventGraph) {
        this.getEventGraph = getEventGraph;
    }

    @GetMapping
    public EventGraphView getGraph(
            @RequestParam(required = false) Set<String> timelineIds) {
        return getEventGraph.execute(new GetEventGraphQuery(timelineIds));
    }
}
