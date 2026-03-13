package com.chronicle.domain.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * First-class domain model for the story graph.
 * Encapsulates nodes (story events) and edges (connections) and provides
 * graph traversal operations such as BFS shortest-path finding.
 *
 * This is the central abstraction of the storytelling engine — all graph
 * algorithms operate on this model, independent of persistence or framework.
 */
public class StoryGraph {

    private final Map<String, StoryNode> nodes;
    private final List<StoryEdge> edges;
    private final Map<String, List<StoryEdge>> adjacency; // nodeId → outgoing edges

    private StoryGraph(Map<String, StoryNode> nodes, List<StoryEdge> edges) {
        this.nodes = Collections.unmodifiableMap(nodes);
        this.edges = Collections.unmodifiableList(edges);
        this.adjacency = buildAdjacency(edges);
    }

    public static StoryGraph of(List<StoryNode> nodes, List<StoryEdge> edges) {
        Map<String, StoryNode> nodeMap = new LinkedHashMap<>();
        for (StoryNode n : nodes) {
            nodeMap.put(n.id(), n);
        }
        return new StoryGraph(nodeMap, new ArrayList<>(edges));
    }

    public Collection<StoryNode> getNodes() {
        return nodes.values();
    }

    public List<StoryEdge> getEdges() {
        return edges;
    }

    public Optional<StoryNode> findNode(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public boolean containsNode(String id) {
        return nodes.containsKey(id);
    }

    /**
     * BFS shortest-path from {@code fromId} to {@code toId}.
     * Traverses all edges (both explicit and inferred).
     * Returns empty if either node is missing or no path exists.
     */
    public Optional<NarrativeArc> findShortestPath(String fromId, String toId) {
        if (!containsNode(fromId) || !containsNode(toId)) {
            return Optional.empty();
        }
        if (fromId.equals(toId)) {
            return Optional.of(NarrativeArc.singleNode(nodes.get(fromId)));
        }

        Map<String, String> prev = new HashMap<>();
        Map<String, StoryEdge> prevEdge = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(fromId);
        visited.add(fromId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(toId)) {
                return Optional.of(reconstructPath(fromId, toId, prev, prevEdge));
            }
            for (StoryEdge edge : adjacency.getOrDefault(current, List.of())) {
                String next = edge.targetNodeId();
                if (!visited.contains(next)) {
                    visited.add(next);
                    prev.put(next, current);
                    prevEdge.put(next, edge);
                    queue.add(next);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * BFS shortest-path restricted to explicit (non-inferred) edges only.
     * Use this when you want to find narrative paths through user-defined connections.
     */
    public Optional<NarrativeArc> findExplicitPath(String fromId, String toId) {
        if (!containsNode(fromId) || !containsNode(toId)) {
            return Optional.empty();
        }
        if (fromId.equals(toId)) {
            return Optional.of(NarrativeArc.singleNode(nodes.get(fromId)));
        }

        // Build adjacency restricted to explicit edges only
        Map<String, List<StoryEdge>> explicitAdj = new HashMap<>();
        for (StoryEdge edge : edges) {
            if (edge.isExplicit()) {
                explicitAdj.computeIfAbsent(edge.sourceNodeId(), k -> new ArrayList<>()).add(edge);
            }
        }

        Map<String, String> prev = new HashMap<>();
        Map<String, StoryEdge> prevEdge = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(fromId);
        visited.add(fromId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(toId)) {
                return Optional.of(reconstructPath(fromId, toId, prev, prevEdge));
            }
            for (StoryEdge edge : explicitAdj.getOrDefault(current, List.of())) {
                String next = edge.targetNodeId();
                if (!visited.contains(next)) {
                    visited.add(next);
                    prev.put(next, current);
                    prevEdge.put(next, edge);
                    queue.add(next);
                }
            }
        }

        return Optional.empty();
    }

    private NarrativeArc reconstructPath(String from, String to,
                                          Map<String, String> prev,
                                          Map<String, StoryEdge> prevEdge) {
        List<StoryNode> pathNodes = new ArrayList<>();
        List<StoryEdge> pathEdges = new ArrayList<>();
        String current = to;
        while (!current.equals(from)) {
            pathNodes.add(0, nodes.get(current));
            pathEdges.add(0, prevEdge.get(current));
            current = prev.get(current);
        }
        pathNodes.add(0, nodes.get(from));
        return new NarrativeArc(pathNodes, pathEdges);
    }

    private Map<String, List<StoryEdge>> buildAdjacency(List<StoryEdge> edges) {
        Map<String, List<StoryEdge>> adj = new HashMap<>();
        for (StoryEdge edge : edges) {
            adj.computeIfAbsent(edge.sourceNodeId(), k -> new ArrayList<>()).add(edge);
        }
        return adj;
    }
}
