package com.chronicle.application.auth.login;

import com.chronicle.application.auth.PasswordHasher;
import com.chronicle.application.shared.ApplicationException;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.user.Email;
import com.chronicle.domain.user.UserRepository;

public class LoginUseCase implements UseCase<LoginCommand, LoginResult> {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public LoginUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        var email = new Email(command.email());
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException("Invalid credentials"));
        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new ApplicationException("Invalid credentials");
        }
        return LoginResult.from(user);
    }
}
