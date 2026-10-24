package com.nova.auth;

import com.nova.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public AuthDtos.Message signup(@Valid @RequestBody AuthDtos.Signup request) {
        return service.signup(request);
    }

    @PostMapping("/signin")
    public AuthDtos.Response signin(@Valid @RequestBody AuthDtos.Signin request) {
        return service.signin(request);
    }

    @GetMapping("/me")
    public AuthDtos.Profile me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return service.getProfile(auth.getName());
    }

    @PutMapping("/profile")
    public AuthDtos.Profile updateProfile(Authentication auth, @Valid @RequestBody AuthDtos.UpdateProfile request) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return service.updateProfile(auth.getName(), request);
    }
}
