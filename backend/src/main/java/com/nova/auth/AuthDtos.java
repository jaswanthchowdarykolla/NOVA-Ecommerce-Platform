package com.nova.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record Signup(
            @NotBlank @Size(max = 100) String name,
            @Email @NotBlank @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 100) String password
    ) {}

    public record Signin(
            @Email @NotBlank @Size(max = 254) String email,
            @NotBlank String password
    ) {}

    public record Response(
            long id,
            String name,
            String email,
            String role,
            String token
    ) {}

    public record Message(String message) {}

    public record Profile(
            long id,
            String name,
            String email,
            String role
    ) {
        public static Profile from(User user) {
            return new Profile(user.getId() != null ? user.getId() : 0L, user.getName(), user.getEmail(), user.getRole().name());
        }
    }

    public record UpdateProfile(
            @NotBlank @Size(max = 100) String name,
            @Size(min = 8, max = 100) String newPassword
    ) {}
}
