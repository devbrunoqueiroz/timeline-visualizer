package com.chronicle.domain.user;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByEmail(Email email);
    Optional<User> findById(UserId id);
    boolean existsByEmail(Email email);
}
