package com.goaltracker.dto;

import com.goaltracker.entity.TaskFrequency;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * goalTitle / domainName:額外附上父目標標題跟面向名稱,
 * 讓「今日任務」頁面不用為了顯示「這個任務屬於哪個目標/面向」而多打好幾支 API。
 * 這是 DTO 設計常見的取捨:多花一點 Service 層組裝的力氣,換前端少打很多 API、畫面更順。
 */
public record TaskResponse(
        Long id,
        Long goalId,
        String goalTitle,
        Long domainId,
        String domainName,
        String title,
        TaskFrequency frequency,
        LocalDate taskDate,
        Boolean completed,
        LocalDateTime completedAt
) {
}
