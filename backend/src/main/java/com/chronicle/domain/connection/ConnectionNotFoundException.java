package com.chronicle.domain.connection;

import com.chronicle.domain.shared.DomainException;

public class ConnectionNotFoundException extends DomainException {

    public ConnectionNotFoundException(ConnectionId id) {
        super("Connection not found: " + id.value());
    }
}
