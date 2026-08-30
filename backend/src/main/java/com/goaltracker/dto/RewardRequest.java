package com.goaltracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RewardRequest(
        @NotBlank(message = "獎勵名稱不能是空的")
        String title,

        @Positive(message = "所需金幣要大於 0")
        Integer cost
) {
}
