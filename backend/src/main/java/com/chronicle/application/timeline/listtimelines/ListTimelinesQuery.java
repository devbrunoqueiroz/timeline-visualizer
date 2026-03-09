package com.chronicle.application.timeline.listtimelines;

import com.chronicle.domain.timeline.TimelineFilter;
import com.chronicle.domain.user.UserId;

public record ListTimelinesQuery(TimelineFilter filter) {

    public static ListTimelinesQuery all() {
        return new ListTimelinesQuery(TimelineFilter.noFilter());
    }

    public static ListTimelinesQuery forOwner(UserId ownerId) {
        return new ListTimelinesQuery(TimelineFilter.byOwner(ownerId));
    }
}
