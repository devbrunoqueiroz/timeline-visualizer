package com.chronicle.application.story.getstorygraph;

import java.util.List;

public record StoryGraphView(List<StoryGraphNodeView> nodes, List<StoryGraphEdgeView> edges) {
}
