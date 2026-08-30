package com.goaltracker.dto;

import java.time.LocalDate;

public record CompletionLogResponse(
        Long id,
        Long taskId,
        String taskTitle,
        Long goalId,
        String goalTitle,
        Long domainId,
        String domainName,
        LocalDate completedDate,
        String note
) {
}
