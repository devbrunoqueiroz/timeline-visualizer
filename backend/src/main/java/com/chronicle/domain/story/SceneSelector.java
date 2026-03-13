package com.chronicle.domain.story;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SceneSelector {

    private static final Random RANDOM = new Random();

    public Optional<Scene> select(List<Scene> availableScenes, SelectionStrategy strategy) {
        if (availableScenes.isEmpty()) {
            return Optional.empty();
        }
        return switch (strategy) {
            case HIGHEST_PRIORITY -> availableScenes.stream()
                .max((a, b) -> a.getPriority().compareTo(b.getPriority()));
            case DRAMATIC_TENSION -> availableScenes.stream()
                .max((a, b) -> Integer.compare(a.getEffects().size(), b.getEffects().size()));
            case WEIGHTED_RANDOM -> {
                int totalWeight = availableScenes.stream()
                    .mapToInt(s -> s.getPriority().value() + 1)
                    .sum();
                int roll = RANDOM.nextInt(totalWeight);
                int cumulative = 0;
                for (var scene : availableScenes) {
                    cumulative += scene.getPriority().value() + 1;
                    if (roll < cumulative) {
                        yield Optional.of(scene);
                    }
                }
                yield Optional.of(availableScenes.get(0));
            }
        };
    }
}
