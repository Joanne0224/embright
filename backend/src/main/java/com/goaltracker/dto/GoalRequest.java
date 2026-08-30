package com.goaltracker.dto;

import com.goaltracker.entity.GoalStatus;
import com.goaltracker.entity.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 新增/編輯目標的請求格式。
 *
 * parentId 規則(由 Service 層驗證,不是資料庫層):
 * - type=LONG 時,parentId 必須是 null
 * - type=MID 時,parentId 必須指向一個存在、且 type=LONG 的目標
 * - type=SHORT 時,parentId 必須指向一個存在、且 type=MID 的目標
 * 這種「跨欄位邏輯」資料庫的 CHECK 約束很難優雅表達,所以放在 Service 層用 Java 邏輯把關。
 */
public record GoalRequest(
        @NotNull(message = "一定要指定屬於哪個面向")
        Long domainId,

        Long parentId,

        @NotNull(message = "一定要指定是長期/中期/短期目標")
        GoalType type,

        @NotBlank(message = "目標標題不能是空的")
        String title,

        String description,

        LocalDate targetDate,

        GoalStatus status
) {
}
