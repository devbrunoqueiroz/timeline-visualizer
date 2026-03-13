package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

public record Effect(EffectType type, String factKey, String factValue) {

    public Effect {
        if (factKey == null || factKey.isBlank()) {
            throw new DomainException("Effect factKey cannot be blank");
        }
    }

    public static Effect addFact(String factKey) {
        return new Effect(EffectType.ADD_FACT, factKey, null);
    }

    public static Effect addFact(String factKey, String factValue) {
        return new Effect(EffectType.ADD_FACT, factKey, factValue);
    }

    public static Effect removeFact(String factKey) {
        return new Effect(EffectType.REMOVE_FACT, factKey, null);
    }

    public static Effect setFact(String factKey, String factValue) {
        return new Effect(EffectType.SET_FACT, factKey, factValue);
    }
}
