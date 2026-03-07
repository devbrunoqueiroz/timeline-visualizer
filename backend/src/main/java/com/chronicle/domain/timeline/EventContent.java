package com.chronicle.domain.timeline;

import java.util.Map;
import java.util.Objects;

public record EventContent(String text, ContentType type, Map<String, String> metadata) {

    public EventContent {
        Objects.requireNonNull(text, "Content text cannot be null");
        Objects.requireNonNull(type, "Content type cannot be null");
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public static EventContent text(String text) {
        return new EventContent(text, ContentType.TEXT, Map.of());
    }

    public static EventContent richText(String text, Map<String, String> metadata) {
        return new EventContent(text, ContentType.RICH_TEXT, metadata);
    }

    public static EventContent markdown(String text) {
        return new EventContent(text, ContentType.MARKDOWN, Map.of());
    }
}
