package com.goaltracker.dto;

public record AuthResponse(
        String token,
        Long userId,
        String username
) {
}
