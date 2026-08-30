package com.goaltracker.dto;

public record RewardResponse(
        Long id,
        String title,
        Integer cost,
        boolean affordable // 目前餘額夠不夠兌換這個獎勵,前端可以直接依這個值決定按鈕能不能按
) {
}
