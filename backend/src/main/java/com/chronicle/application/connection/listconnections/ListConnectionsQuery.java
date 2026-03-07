package com.chronicle.application.connection.listconnections;

import java.util.Set;

public record ListConnectionsQuery(Set<String> eventIds) {
}
