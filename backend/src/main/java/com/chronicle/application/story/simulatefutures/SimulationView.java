package com.chronicle.application.story.simulatefutures;

import java.util.List;
import java.util.Map;

public record SimulationView(
    List<String> sceneIds,
    List<String> sceneTitles,
    Map<String, String> finalWorldState,
    int depth
) {}
