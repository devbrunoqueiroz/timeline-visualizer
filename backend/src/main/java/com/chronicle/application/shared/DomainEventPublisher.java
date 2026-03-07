package com.chronicle.application.shared;

import com.chronicle.domain.shared.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
