package com.chronicle.application.auth.register;

import com.chronicle.application.auth.PasswordHasher;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.user.Email;
import com.chronicle.domain.user.User;
import com.chronicle.domain.user.UserAlreadyExistsException;
import com.chronicle.domain.user.UserRepository;

public class RegisterUserUseCase implements UseCase<RegisterUserCommand, RegisterUserResult> {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public RegisterUserResult execute(RegisterUserCommand command) {
        var email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email.value());
        }
        var passwordHash = passwordHasher.hash(command.password());
        var user = User.create(email, passwordHash);
        userRepository.save(user);
        return RegisterUserResult.from(user);
    }
}
