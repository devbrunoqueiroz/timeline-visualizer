package com.chronicle.domain.shared;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
