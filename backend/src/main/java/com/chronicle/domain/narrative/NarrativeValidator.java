package com.chronicle.domain.narrative;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.timeline.TemporalPosition;

import java.util.List;

/**
 * Pure domain service — no external dependencies.
 * Validates a connection between two events given their temporal positions.
 */
public class NarrativeValidator {

    public List<NarrativeValidationResult> validate(String connectionId,
                                                     ConnectionType connectionType,
                                                     TemporalPosition source,
                                                     TemporalPosition target) {
        return switch (connectionType) {
            case FORESHADOW -> ordered(connectionId, source, target,
                    "FORESHADOW: the origin event occurs after the foreshadowed event — temporal order is inverted.",
                    "Swap source and target, or reposition the origin event earlier in the timeline.");

            case REVEAL -> ordered(connectionId, source, target,
                    "REVEAL: the revealed event occurs after the reveal moment — the secret should come first.",
                    "Ensure the source (hidden event) precedes the target (reveal moment).");

            case PREREQUISITE -> ordered(connectionId, source, target,
                    "PREREQUISITE: the prerequisite occurs after the event that depends on it.",
                    "Move the prerequisite event earlier in the timeline.");

            case ESCALATION -> ordered(connectionId, source, target,
                    "ESCALATION: the escalation precedes the event it escalates from — intensity should increase forward.",
                    "Reorder events so narrative intensity increases over time.");

            case RESOLUTION -> ordered(connectionId, source, target,
                    "RESOLUTION: the resolution occurs before the conflict it resolves.",
                    "Ensure the conflict event is placed earlier than the resolution.");

            case CAUSAL -> ordered(connectionId, source, target,
                    "CAUSAL: the cause occurs after its effect — temporal order is inverted.",
                    "Move the cause event earlier, or swap source and target.");

            case TEMPORAL -> strict(connectionId, source, target);

            case PARALLEL, CONTRAST, REFERENCE -> List.of();
        };
    }

    /** source must be strictly before target (source.position < target.position). */
    private List<NarrativeValidationResult> ordered(String connectionId,
                                                     TemporalPosition source, TemporalPosition target,
                                                     String message, String suggestedFix) {
        if (!canCompare(source, target)) return List.of();
        if (source.position().compareTo(target.position()) > 0) {
            return List.of(new NarrativeValidationResult(connectionId, ValidationSeverity.WARNING, message, suggestedFix));
        }
        return List.of();
    }

    /** source must be strictly before target — equals also triggers warning. */
    private List<NarrativeValidationResult> strict(String connectionId,
                                                    TemporalPosition source, TemporalPosition target) {
        if (!canCompare(source, target)) return List.of();
        if (source.position().compareTo(target.position()) >= 0) {
            return List.of(new NarrativeValidationResult(connectionId, ValidationSeverity.WARNING,
                    "TEMPORAL: source event does not strictly precede the target — chronological order is broken.",
                    "Adjust temporal positions so the source event occurs strictly before the target."));
        }
        return List.of();
    }

    private boolean canCompare(TemporalPosition source, TemporalPosition target) {
        return source != null && target != null
                && source.position() != null && target.position() != null;
    }
}
