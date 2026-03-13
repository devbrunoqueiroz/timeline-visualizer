package com.chronicle.domain.graph;

import java.util.List;

/**
 * Value object representing an ordered path through the story graph.
 * A narrative arc is a sequence of nodes connected by edges, forming a coherent story thread.
 */
public record NarrativeArc(List<StoryNode> nodes, List<StoryEdge> edges) {

    public NarrativeArc {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }

    public int length() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public static NarrativeArc empty() {
        return new NarrativeArc(List.of(), List.of());
    }

    public static NarrativeArc singleNode(StoryNode node) {
        return new NarrativeArc(List.of(node), List.of());
    }
}
