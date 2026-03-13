package com.chronicle.application.story.advancestory;

import com.chronicle.domain.story.SelectionStrategy;

public record AdvanceStoryCommand(String sessionId, SelectionStrategy strategy) {}
