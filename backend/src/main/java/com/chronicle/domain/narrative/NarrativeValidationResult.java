package com.chronicle.domain.narrative;

public record NarrativeValidationResult(
        String connectionId,
        ValidationSeverity severity,
        String message,
        String suggestedFix) {
}
