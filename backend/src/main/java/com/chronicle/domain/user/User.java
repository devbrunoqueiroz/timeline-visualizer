package com.chronicle.domain.user;

import com.chronicle.domain.shared.AggregateRoot;

import java.time.Instant;

public class User extends AggregateRoot<UserId> {

    private final UserId id;
    private final Email email;
    private final String passwordHash;
    private final Instant createdAt;

    private User(UserId id, Email email, String passwordHash, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public static User create(Email email, String passwordHash) {
        return new User(UserId.generate(), email, passwordHash, Instant.now());
    }

    public static User reconstitute(UserId id, Email email, String passwordHash, Instant createdAt) {
        return new User(id, email, passwordHash, createdAt);
    }

    @Override
    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
}
