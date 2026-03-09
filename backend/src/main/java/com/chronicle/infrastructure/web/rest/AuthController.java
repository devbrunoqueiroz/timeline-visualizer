package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.auth.login.LoginCommand;
import com.chronicle.application.auth.login.LoginUseCase;
import com.chronicle.application.auth.register.RegisterUserCommand;
import com.chronicle.application.auth.register.RegisterUserUseCase;
import com.chronicle.infrastructure.security.JwtTokenProvider;
import com.chronicle.infrastructure.web.rest.dto.AuthResponse;
import com.chronicle.infrastructure.web.rest.dto.LoginRequest;
import com.chronicle.infrastructure.web.rest.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;
    private final JwtTokenProvider tokenProvider;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase login, JwtTokenProvider tokenProvider) {
        this.registerUser = registerUser;
        this.login = login;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        var result = registerUser.execute(new RegisterUserCommand(request.email(), request.password()));
        var token = tokenProvider.generateToken(UUID.fromString(result.userId()), result.email());
        return new AuthResponse(token, result.userId(), result.email());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        var result = login.execute(new LoginCommand(request.email(), request.password()));
        var token = tokenProvider.generateToken(UUID.fromString(result.userId()), result.email());
        return new AuthResponse(token, result.userId(), result.email());
    }
}
