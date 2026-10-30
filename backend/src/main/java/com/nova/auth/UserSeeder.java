package com.nova.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UserSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmailIgnoreCase("demo@example.com")) {
            users.save(new User("Demo Customer", "demo@example.com", encoder.encode("Password123!"), Role.CUSTOMER));
        }
        if (!users.existsByEmailIgnoreCase("admin@example.com")) {
            users.save(new User("NOVA Admin", "admin@example.com", encoder.encode("AdminPassword123!"), Role.ADMIN));
        }
    }
}

