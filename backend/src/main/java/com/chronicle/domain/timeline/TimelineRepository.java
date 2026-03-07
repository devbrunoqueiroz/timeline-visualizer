package com.chronicle.domain.timeline;

import java.util.List;
import java.util.Optional;

public interface TimelineRepository {
    void save(Timeline timeline);
    Optional<Timeline> findById(TimelineId id);
    List<Timeline> findAll(TimelineFilter filter);
    void delete(TimelineId id);
    boolean existsById(TimelineId id);
}
