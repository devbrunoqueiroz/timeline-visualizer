package com.chronicle.application.timeline.listtimelines;

import com.chronicle.domain.timeline.TimelineFilter;

public record ListTimelinesQuery(TimelineFilter filter) {

    public static ListTimelinesQuery all() {
        return new ListTimelinesQuery(TimelineFilter.noFilter());
    }
}
