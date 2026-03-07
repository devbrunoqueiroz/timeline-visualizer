package com.chronicle.application.graph.geteventgraph;

import com.chronicle.domain.narrative.NarrativeValidationResult;

import java.util.List;

public record EventGraphView(List<GraphNodeView> nodes,
                              List<GraphEdgeView> edges,
                              List<NarrativeValidationResult> validations) {
}
