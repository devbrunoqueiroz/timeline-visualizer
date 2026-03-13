package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

public class SceneNotFoundException extends DomainException {

    public SceneNotFoundException(SceneId id) {
        super("Scene not found: " + id.value());
    }
}
