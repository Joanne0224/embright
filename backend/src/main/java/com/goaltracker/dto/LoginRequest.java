package com.goaltracker.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "帳號不能是空的")
        String username,

        @NotBlank(message = "密碼不能是空的")
        String password
) {
}
