package com.chronicle.domain.story;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WorldState {

    private final Map<String, String> facts;

    private WorldState(Map<String, String> facts) {
        this.facts = Map.copyOf(facts);
    }

    public static WorldState empty() {
        return new WorldState(Map.of());
    }

    public static WorldState of(Map<String, String> facts) {
        return new WorldState(facts);
    }

    public boolean hasFact(String key) {
        return facts.containsKey(key);
    }

    public Optional<String> getFactValue(String key) {
        return Optional.ofNullable(facts.get(key));
    }

    public boolean satisfies(Requirement requirement) {
        return switch (requirement.type()) {
            case FACT_EXISTS -> hasFact(requirement.factKey());
            case FACT_ABSENT -> !hasFact(requirement.factKey());
            case FACT_EQUALS -> hasFact(requirement.factKey()) &&
                    facts.get(requirement.factKey()).equals(requirement.expectedValue());
        };
    }

    public boolean satisfiesAll(List<Requirement> requirements) {
        return requirements.stream().allMatch(this::satisfies);
    }

    public WorldState apply(Effect effect) {
        var newFacts = new HashMap<>(facts);
        switch (effect.type()) {
            case ADD_FACT, SET_FACT -> newFacts.put(effect.factKey(),
                    effect.factValue() != null ? effect.factValue() : "");
            case REMOVE_FACT -> newFacts.remove(effect.factKey());
        }
        return new WorldState(newFacts);
    }

    public WorldState applyAll(List<Effect> effects) {
        var result = this;
        for (var effect : effects) {
            result = result.apply(effect);
        }
        return result;
    }

    public Map<String, String> getFacts() {
        return facts;
    }
}
