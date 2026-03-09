package com.chronicle.application.auth.login;

import com.chronicle.domain.user.User;

public record LoginResult(String userId, String email) {
    public static LoginResult from(User user) {
        return new LoginResult(user.getId().toString(), user.getEmail().value());
    }
}
