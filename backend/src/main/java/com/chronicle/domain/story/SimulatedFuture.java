package com.chronicle.domain.story;

import java.util.List;

public record SimulatedFuture(
    List<SceneId> scenePath,
    List<String> sceneTitles,
    WorldState finalWorldState,
    int depth
) {}
