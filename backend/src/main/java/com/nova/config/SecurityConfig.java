package com.nova.config;

import com.nova.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwt;
    private final ApiAuthenticationEntryPoint authenticationEntryPoint;
    private final ApiAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwt,
                          ApiAuthenticationEntryPoint authenticationEntryPoint,
                          ApiAccessDeniedHandler accessDeniedHandler) {
        this.jwt = jwt;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/api/health",
                                "/actuator/health",
                                "/api/auth/signup",
                                "/api/auth/signin",
                                "/api/products",
                                "/api/products/**",
                                "/api/assistant/**",
                                "/api/payments/webhook"
                        ).permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/jobs", "/api/jobs/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/auth/me",
                                "/api/auth/profile",
                                "/api/addresses",
                                "/api/addresses/**",
                                "/api/orders",
                                "/api/orders/**",
                                "/api/payments",
                                "/api/payments/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
