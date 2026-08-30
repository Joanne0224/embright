package com.goaltracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "帳號不能是空的")
        @Size(min = 3, max = 20, message = "帳號長度要在 3~20 字之間")
        String username,

        @NotBlank(message = "密碼不能是空的")
        @Size(min = 6, message = "密碼至少要 6 個字元")
        String password
) {
}
