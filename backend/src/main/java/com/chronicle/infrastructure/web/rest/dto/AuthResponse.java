package com.chronicle.infrastructure.web.rest.dto;

public record AuthResponse(String token, String userId, String email) {}
