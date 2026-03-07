package com.chronicle.domain.connection;

public enum ConnectionType {
    // Original types
    CAUSAL,
    TEMPORAL,
    REFERENCE,
    CONTRAST,
    // Narrative enrichment types
    PREREQUISITE,
    FORESHADOW,
    REVEAL,
    ESCALATION,
    RESOLUTION,
    PARALLEL
}
