package com.chronicle.application.graph.findstorypath;

import java.util.Objects;

/**
 * Query to find a story path between two events in the narrative graph.
 *
 * @param fromEventId  source event ID (UUID string)
 * @param toEventId    target event ID (UUID string)
 * @param explicitOnly if true, only traverse user-defined connections; if false, also use inferred temporal edges
 */
public record FindStoryPathQuery(String fromEventId, String toEventId, boolean explicitOnly) {

    public FindStoryPathQuery {
        Objects.requireNonNull(fromEventId, "fromEventId is required");
        Objects.requireNonNull(toEventId, "toEventId is required");
    }

    public static FindStoryPathQuery between(String from, String to) {
        return new FindStoryPathQuery(from, to, false);
    }

    public static FindStoryPathQuery explicitBetween(String from, String to) {
        return new FindStoryPathQuery(from, to, true);
    }
}
