package com.goaltracker.dto;

import com.goaltracker.entity.CoinReason;

import java.time.LocalDateTime;

public record CoinTransactionResponse(
        Long id,
        Integer amount,
        CoinReason reason,
        String note,
        LocalDateTime createdAt
) {
}
