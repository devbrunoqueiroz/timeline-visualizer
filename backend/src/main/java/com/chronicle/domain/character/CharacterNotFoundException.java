package com.chronicle.domain.character;

import com.chronicle.application.shared.ApplicationException;

public class CharacterNotFoundException extends ApplicationException {
    public CharacterNotFoundException(String id) {
        super("Character not found: " + id);
    }
}
