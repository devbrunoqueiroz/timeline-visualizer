package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

public record Requirement(String factKey, RequirementType type, String expectedValue) {

    public Requirement {
        if (factKey == null || factKey.isBlank()) {
            throw new DomainException("Requirement factKey cannot be blank");
        }
    }

    public static Requirement factExists(String factKey) {
        return new Requirement(factKey, RequirementType.FACT_EXISTS, null);
    }

    public static Requirement factAbsent(String factKey) {
        return new Requirement(factKey, RequirementType.FACT_ABSENT, null);
    }

    public static Requirement factEquals(String factKey, String expectedValue) {
        return new Requirement(factKey, RequirementType.FACT_EQUALS, expectedValue);
    }
}
