package com.example.ara.dto;

public record AuthResponse(
    String token,
    String email,
    String fullName
) {}
