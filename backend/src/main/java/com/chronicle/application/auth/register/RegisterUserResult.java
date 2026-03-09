package com.chronicle.application.auth.register;

import com.chronicle.domain.user.User;

public record RegisterUserResult(String userId, String email) {
    public static RegisterUserResult from(User user) {
        return new RegisterUserResult(user.getId().toString(), user.getEmail().value());
    }
}
