package com.chronicle.application.narrative;

import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.narrative.NarrativeValidationResult;
import com.chronicle.domain.narrative.NarrativeValidator;
import com.chronicle.domain.timeline.TemporalPosition;

import java.util.List;

public class NarrativeValidationService {

    private final NarrativeValidator validator = new NarrativeValidator();

    public List<NarrativeValidationResult> validate(String connectionId,
                                                     ConnectionType connectionType,
                                                     TemporalPosition sourcePosition,
                                                     TemporalPosition targetPosition) {
        return validator.validate(connectionId, connectionType, sourcePosition, targetPosition);
    }
}
