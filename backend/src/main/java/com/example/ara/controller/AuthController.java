package com.example.ara.controller;

import com.example.ara.dto.*;
import com.example.ara.model.PasswordResetToken;
import com.example.ara.model.User;
import com.example.ara.repository.PasswordResetTokenRepository;
import com.example.ara.service.EmailService;
import com.example.ara.service.JwtService;
import com.example.ara.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          PasswordResetTokenRepository resetTokenRepository,
                          EmailService emailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.resetTokenRepository = resetTokenRepository;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPassword(passwordEncoder.encode(request.password()));
        User saved = userService.save(user);
        String token = jwtService.generateToken(saved);
        return new AuthResponse(token, saved.getEmail(), saved.getFullName());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            User user = (User) auth.getPrincipal();
            String token = jwtService.generateToken(user);
            return new AuthResponse(token, user.getEmail(), user.getFullName());
        } catch (BadCredentialsException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        // Always return 200 — never reveal whether the email is registered
        userService.findByEmail(request.email()).ifPresent(user -> {
            resetTokenRepository.deleteAllByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            resetTokenRepository.save(new PasswordResetToken(token, user));
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
        });
        return ResponseEntity.ok(Map.of("message",
            "If that email is registered, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid or expired reset link."));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "This reset link has expired or already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return Map.of("email", user.getEmail(), "fullName", user.getFullName());
    }
}
