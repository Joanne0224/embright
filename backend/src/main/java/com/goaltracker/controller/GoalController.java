package com.goaltracker.controller;

import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.GoalRequest;
import com.goaltracker.dto.GoalResponse;
import com.goaltracker.entity.GoalType;
import com.goaltracker.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    // GET /api/goals?domainId=1&type=LONG -> 目標地圖頁第一層:某面向的所有長期目標
    @GetMapping
    public ApiResponse<List<GoalResponse>> getByDomainAndType(
            @RequestParam Long domainId,
            @RequestParam GoalType type) {
        return ApiResponse.ok(goalService.getByDomainAndType(domainId, type));
    }

    // GET /api/goals/{id}/children -> 展開某個目標,看它底下的子目標
    @GetMapping("/{id}/children")
    public ApiResponse<List<GoalResponse>> getChildren(@PathVariable Long id) {
        return ApiResponse.ok(goalService.getChildren(id));
    }

    // POST /api/goals -> 新增目標(長/中/短期都是這支API,靠 type + parentId 區分)
    @PostMapping
    public ApiResponse<GoalResponse> create(@Valid @RequestBody GoalRequest request) {
        return ApiResponse.ok("目標已新增", goalService.create(request));
    }

    // PUT /api/goals/{id} -> 編輯目標內容
    @PutMapping("/{id}")
    public ApiResponse<GoalResponse> update(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        return ApiResponse.ok("目標已更新", goalService.update(id, request));
    }

    // PATCH /api/goals/{id}/complete -> 手動標記整個目標達成
    @PatchMapping("/{id}/complete")
    public ApiResponse<GoalResponse> markCompleted(@PathVariable Long id) {
        return ApiResponse.ok("目標已標記為達成", goalService.markCompleted(id));
    }

    // DELETE /api/goals/{id} -> 刪除目標(連同底下子目標、任務一起刪除)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        goalService.delete(id);
        return ApiResponse.ok("目標已刪除", null);
    }
}
