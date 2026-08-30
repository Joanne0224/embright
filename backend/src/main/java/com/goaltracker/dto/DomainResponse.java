package com.goaltracker.dto;

/**
 * 回給前端的面向資料。
 * longGoalCount:這個面向底下有幾個長期目標,首頁卡片可以直接顯示,不用前端另外算。
 */
public record DomainResponse(
        Long id,
        String name,
        String color,
        Integer sortOrder,
        long longGoalCount
) {
}
