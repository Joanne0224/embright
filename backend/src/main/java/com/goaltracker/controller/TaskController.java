package com.goaltracker.controller;

import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.TaskRequest;
import com.goaltracker.dto.TaskResponse;
import com.goaltracker.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks?goalId=5 -> 某個短期目標底下的所有任務
    @GetMapping
    public ApiResponse<List<TaskResponse>> getByGoalId(@RequestParam Long goalId) {
        return ApiResponse.ok(taskService.getByGoalId(goalId));
    }

    // GET /api/tasks/today -> 今日任務(跨所有面向彙整)
    @GetMapping("/today")
    public ApiResponse<List<TaskResponse>> getToday() {
        return ApiResponse.ok(taskService.getByDate(LocalDate.now()));
    }

    // GET /api/tasks/date/2026-08-25 -> 查指定日期的任務(方便老師測試不同日期)
    @GetMapping("/date/{date}")
    public ApiResponse<List<TaskResponse>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(taskService.getByDate(date));
    }

    // POST /api/tasks -> 新增每日/每週任務
    @PostMapping
    public ApiResponse<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ApiResponse.ok("任務已新增", taskService.create(request));
    }

    // PATCH /api/tasks/{id}/complete -> 打勾完成(同時寫入completion_logs)
    @PatchMapping("/{id}/complete")
    public ApiResponse<TaskResponse> complete(@PathVariable Long id) {
        return ApiResponse.ok("任務已完成", taskService.complete(id));
    }

    // PATCH /api/tasks/{id}/uncomplete -> 取消完成(誤按可復原)
    @PatchMapping("/{id}/uncomplete")
    public ApiResponse<TaskResponse> uncomplete(@PathVariable Long id) {
        return ApiResponse.ok("已取消完成", taskService.uncomplete(id));
    }

    // DELETE /api/tasks/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ApiResponse.ok("任務已刪除", null);
    }
}
