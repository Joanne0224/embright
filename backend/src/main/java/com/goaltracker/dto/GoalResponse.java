package com.goaltracker.dto;

import com.goaltracker.entity.GoalStatus;
import com.goaltracker.entity.GoalType;

import java.time.LocalDate;

/**
 * 回給前端的目標資料。
 * childCount:底下有幾個子目標(中期目標底下有幾個短期目標之類的),前端畫樹狀圖時可以直接用,
 * 不用另外呼叫 API 才知道「這個節點展不展得開」。
 */
public record GoalResponse(
        Long id,
        Long domainId,
        Long parentId,
        GoalType type,
        String title,
        String description,
        LocalDate targetDate,
        GoalStatus status,
        long childCount
) {
}
