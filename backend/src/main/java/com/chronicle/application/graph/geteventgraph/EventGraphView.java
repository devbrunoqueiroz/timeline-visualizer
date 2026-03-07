package com.chronicle.application.graph.geteventgraph;

import java.util.List;

public record EventGraphView(List<GraphNodeView> nodes, List<GraphEdgeView> edges) {
}
