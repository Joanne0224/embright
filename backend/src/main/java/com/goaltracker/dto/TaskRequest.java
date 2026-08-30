package com.goaltracker.dto;

import com.goaltracker.entity.TaskFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskRequest(
        @NotNull(message = "一定要指定屬於哪個目標")
        Long goalId,

        @NotBlank(message = "任務標題不能是空的")
        String title,

        @NotNull(message = "一定要指定頻率(每日/每週/一次性)")
        TaskFrequency frequency,

        @NotNull(message = "一定要指定日期")
        LocalDate taskDate
) {
}
