package com.goaltracker.controller;

import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.CompletionLogResponse;
import com.goaltracker.service.CompletionLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/completion-logs")
public class CompletionLogController {

    private final CompletionLogService completionLogService;

    public CompletionLogController(CompletionLogService completionLogService) {
        this.completionLogService = completionLogService;
    }

    // GET /api/completion-logs -> 完成紀錄頁:全部歷史(這支是老師會當場測試的核心查詢功能)
    @GetMapping
    public ApiResponse<List<CompletionLogResponse>> getAll(
            @RequestParam(required = false) Long domainId,
            @RequestParam(required = false) Long goalId) {
        if (domainId != null) {
            return ApiResponse.ok(completionLogService.getByDomain(domainId));
        }
        if (goalId != null) {
            return ApiResponse.ok(completionLogService.getByGoal(goalId));
        }
        return ApiResponse.ok(completionLogService.getAll());
    }
}
