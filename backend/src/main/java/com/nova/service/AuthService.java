package com.nova.service;

import com.nova.auth.AuthDtos;
import com.nova.auth.Role;
import com.nova.auth.User;
import com.nova.auth.UserRepository;
import com.nova.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthDtos.Message signup(AuthDtos.Signup request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        users.save(new User(request.name().trim(), email, encoder.encode(request.password()), Role.CUSTOMER));
        return new AuthDtos.Message("Account created. Sign in to continue.");
    }

    public AuthDtos.Response signin(AuthDtos.Signin request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new AuthDtos.Response(user.getId(), user.getName(), user.getEmail(), user.getRole().name(), jwt.generate(user));
    }

    @Transactional(readOnly = true)
    public AuthDtos.Profile getProfile(String email) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        return AuthDtos.Profile.from(user);
    }

    @Transactional
    public AuthDtos.Profile updateProfile(String email, AuthDtos.UpdateProfile request) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        user.setName(request.name().trim());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.newPassword().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
            }
            user.setPassword(encoder.encode(request.newPassword()));
        }
        return AuthDtos.Profile.from(user);
    }
}
