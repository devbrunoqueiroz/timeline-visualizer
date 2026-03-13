package com.chronicle.domain.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pure domain service for graph traversal operations.
 * No external framework dependencies — operates exclusively on domain objects.
 *
 * Provides:
 * - Shortest path between two events (BFS, all edges)
 * - Explicit narrative path between two events (BFS, explicit connections only)
 * - Linear arc detection from root nodes (nodes with no incoming explicit connections)
 */
public class GraphTraversalService {

    /**
     * Finds the shortest path between two events in the full graph
     * (traverses both explicit connections and inferred temporal edges).
     */
    public Optional<NarrativeArc> findShortestPath(StoryGraph graph, String fromId, String toId) {
        return graph.findShortestPath(fromId, toId);
    }

    /**
     * Finds a path using only explicit (user-defined) connections.
     * Useful when you want to see how the author explicitly connected events.
     */
    public Optional<NarrativeArc> findExplicitPath(StoryGraph graph, String fromId, String toId) {
        return graph.findExplicitPath(fromId, toId);
    }

    /**
     * Detects linear narrative arcs by following chains of explicit connections
     * starting from "root" nodes — nodes with no incoming explicit edges.
     * Returns one arc per root that leads to at least one other node.
     */
    public List<NarrativeArc> detectLinearArcs(StoryGraph graph) {
        Set<String> hasIncomingExplicit = new HashSet<>();
        for (StoryEdge edge : graph.getEdges()) {
            if (edge.isExplicit()) {
                hasIncomingExplicit.add(edge.targetNodeId());
            }
        }

        List<NarrativeArc> arcs = new ArrayList<>();
        for (StoryNode node : graph.getNodes()) {
            if (!hasIncomingExplicit.contains(node.id())) {
                traceForwardArc(graph, node.id()).ifPresent(arcs::add);
            }
        }
        return arcs;
    }

    /**
     * Follows explicit edges forward from a starting node, collecting nodes
     * into a single arc until a leaf (no outgoing explicit edge) is reached.
     * Returns empty if the arc has only one node (isolated).
     */
    private Optional<NarrativeArc> traceForwardArc(StoryGraph graph, String startId) {
        List<StoryNode> pathNodes = new ArrayList<>();
        List<StoryEdge> pathEdges = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        String current = startId;
        while (current != null && !visited.contains(current)) {
            visited.add(current);
            Optional<StoryNode> node = graph.findNode(current);
            if (node.isEmpty()) break;
            pathNodes.add(node.get());

            StoryEdge nextEdge = findFirstExplicitOutgoing(graph, current);
            if (nextEdge == null) break;
            pathEdges.add(nextEdge);
            current = nextEdge.targetNodeId();
        }

        if (pathNodes.size() <= 1) return Optional.empty();
        return Optional.of(new NarrativeArc(pathNodes, pathEdges));
    }

    private StoryEdge findFirstExplicitOutgoing(StoryGraph graph, String nodeId) {
        return graph.getEdges().stream()
                .filter(e -> e.isExplicit() && e.sourceNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
}
