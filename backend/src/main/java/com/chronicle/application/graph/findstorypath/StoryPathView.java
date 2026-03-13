package com.chronicle.application.graph.findstorypath;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.graph.NarrativeArc;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application-layer DTO for a story path result.
 * Safe to serialize; never exposes domain internals.
 */
public record StoryPathView(
        boolean found,
        int hopCount,
        List<StoryPathNodeView> nodes,
        List<StoryPathEdgeView> edges) {

    public record StoryPathNodeView(
            String id,
            String title,
            String temporalLabel,
            BigDecimal temporalPosition,
            String calendarSystem,
            String timelineId) {
    }

    public record StoryPathEdgeView(
            String id,
            String sourceId,
            String targetId,
            ConnectionType connectionType,
            boolean inferred) {
    }

    public static StoryPathView notFound() {
        return new StoryPathView(false, 0, List.of(), List.of());
    }

    public static StoryPathView from(NarrativeArc arc) {
        var nodes = arc.nodes().stream()
                .map(n -> new StoryPathNodeView(
                        n.id(),
                        n.title(),
                        n.temporalPosition() != null ? n.temporalPosition().label() : null,
                        n.temporalPosition() != null ? n.temporalPosition().position() : null,
                        n.temporalPosition() != null ? n.temporalPosition().calendarSystem() : null,
                        n.timelineId() != null ? n.timelineId().value().toString() : null
                ))
                .toList();

        var edges = arc.edges().stream()
                .map(e -> new StoryPathEdgeView(
                        e.id(),
                        e.sourceNodeId(),
                        e.targetNodeId(),
                        e.connectionType(),
                        e.inferred()
                ))
                .toList();

        return new StoryPathView(true, arc.length() - 1, nodes, edges);
    }
}
