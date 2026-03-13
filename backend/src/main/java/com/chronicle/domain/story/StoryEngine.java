package com.chronicle.domain.story;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class StoryEngine {

    private static final int MAX_SIMULATED_FUTURES = 20;

    public List<Scene> getAvailableScenes(Story story, WorldState worldState) {
        return getAvailableScenes(story, worldState, List.of());
    }

    public List<Scene> getAvailableScenes(Story story, WorldState worldState, List<SceneId> appliedSceneIds) {
        return story.getScenes().stream()
                .filter(scene -> {
                    if (!scene.isRepeatable() && appliedSceneIds.contains(scene.getId())) {
                        return false;
                    }
                    return scene.isAvailable(worldState);
                })
                .toList();
    }

    public WorldState simulateScene(Scene scene, WorldState worldState) {
        return scene.applyTo(worldState);
    }

    public List<NarrativeContradiction> checkConsistency(Scene scene, WorldState worldState) {
        var contradictions = new ArrayList<NarrativeContradiction>();
        for (var effect : scene.getEffects()) {
            if (effect.type() == EffectType.SET_FACT || effect.type() == EffectType.ADD_FACT) {
                if (worldState.hasFact(effect.factKey())) {
                    var existingValue = worldState.getFactValue(effect.factKey()).orElse(null);
                    var newValue = effect.factValue() != null ? effect.factValue() : "";
                    if (existingValue != null && !existingValue.equals(newValue)) {
                        contradictions.add(new NarrativeContradiction(
                                scene.getId(),
                                effect.factKey(),
                                "Scene effect would change fact '" + effect.factKey() +
                                        "' from '" + existingValue + "' to '" + newValue + "'",
                                ContradictionSeverity.WARNING
                        ));
                    }
                }
            }
        }
        return contradictions;
    }

    public List<NarrativeConflict> validateScene(Scene scene, StorySession session) {
        var validator = NarrativeValidator.withDefaultRules();
        return validator.validate(scene, session.getWorldState(), session.getAppliedSceneIds());
    }

    public AdvanceResult advanceStory(Story story, StorySession session, SelectionStrategy strategy) {
        var available = getAvailableScenes(story, session.getWorldState(), session.getAppliedSceneIds());
        if (available.isEmpty()) {
            return AdvanceResult.noScenesAvailable();
        }
        var selector = new SceneSelector();
        var selectedOpt = selector.select(available, strategy);
        if (selectedOpt.isEmpty()) {
            return AdvanceResult.noScenesAvailable();
        }
        var selected = selectedOpt.get();
        var conflicts = validateScene(selected, session);
        var newWorldState = selected.applyTo(session.getWorldState());
        return AdvanceResult.advanced(selected, newWorldState, conflicts);
    }

    public List<SimulatedFuture> simulateFutures(Story story, StorySession session, int maxDepth) {
        var results = new ArrayList<SimulatedFuture>();
        var queue = new LinkedList<SimState>();
        queue.add(new SimState(
            session.getWorldState(),
            new ArrayList<>(session.getAppliedSceneIds()),
            new ArrayList<>(),
            new ArrayList<>(),
            0
        ));

        while (!queue.isEmpty() && results.size() < MAX_SIMULATED_FUTURES) {
            var current = queue.poll();
            var available = getAvailableScenes(story, current.worldState(), current.appliedIds());

            if (current.depth() >= maxDepth || available.isEmpty()) {
                if (!current.path().isEmpty()) {
                    results.add(new SimulatedFuture(
                        List.copyOf(current.path()),
                        List.copyOf(current.titles()),
                        current.worldState(),
                        current.depth()
                    ));
                }
                continue;
            }

            for (var scene : available) {
                if (results.size() >= MAX_SIMULATED_FUTURES) break;
                var nextState = scene.applyTo(current.worldState());
                var nextApplied = new ArrayList<>(current.appliedIds());
                if (!scene.isRepeatable()) nextApplied.add(scene.getId());
                var nextPath = new ArrayList<>(current.path());
                nextPath.add(scene.getId());
                var nextTitles = new ArrayList<>(current.titles());
                nextTitles.add(scene.getTitle());
                queue.add(new SimState(nextState, nextApplied, nextPath, nextTitles, current.depth() + 1));
            }
        }

        return results;
    }

    public List<Scene> getReachableScenes(Story story, WorldState worldState) {
        var reachable = new HashSet<SceneId>();
        var queue = new LinkedList<WorldState>();
        queue.add(worldState);

        var visitedStates = new HashSet<String>();
        visitedStates.add(worldState.getFacts().toString());

        while (!queue.isEmpty()) {
            var currentState = queue.poll();
            var available = getAvailableScenes(story, currentState);

            for (var scene : available) {
                reachable.add(scene.getId());
                var nextState = simulateScene(scene, currentState);
                var stateKey = nextState.getFacts().toString();
                if (!visitedStates.contains(stateKey)) {
                    visitedStates.add(stateKey);
                    queue.add(nextState);
                }
            }
        }

        return story.getScenes().stream()
                .filter(s -> reachable.contains(s.getId()))
                .toList();
    }

    private record SimState(
        WorldState worldState,
        List<SceneId> appliedIds,
        List<SceneId> path,
        List<String> titles,
        int depth
    ) {}
}
