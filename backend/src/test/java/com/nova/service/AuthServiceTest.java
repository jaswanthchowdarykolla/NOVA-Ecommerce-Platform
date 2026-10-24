package com.nova.service;

import com.nova.auth.AuthDtos;
import com.nova.auth.Role;
import com.nova.auth.User;
import com.nova.auth.UserRepository;
import com.nova.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Jane Customer", "jane@example.com", "encoded-pass", Role.CUSTOMER);
    }

    @Test
    void getProfileReturnsExistingUser() {
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));

        AuthDtos.Profile profile = authService.getProfile("jane@example.com");

        assertEquals("Jane Customer", profile.name());
        assertEquals("jane@example.com", profile.email());
        assertEquals("CUSTOMER", profile.role());
    }

    @Test
    void updateProfileUpdatesNameAndValidPassword() {
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("new-secret-123")).thenReturn("new-encoded-pass");

        AuthDtos.UpdateProfile update = new AuthDtos.UpdateProfile("Jane Updated", "new-secret-123");
        AuthDtos.Profile profile = authService.updateProfile("jane@example.com", update);

        assertEquals("Jane Updated", profile.name());
        assertEquals("new-encoded-pass", testUser.getPassword());
    }

    @Test
    void updateProfileRejectsShortPassword() {
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));

        AuthDtos.UpdateProfile update = new AuthDtos.UpdateProfile("Jane Updated", "short");
        assertThrows(ResponseStatusException.class, () -> authService.updateProfile("jane@example.com", update));
    }
}
