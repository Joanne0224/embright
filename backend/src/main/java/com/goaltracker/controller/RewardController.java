package com.goaltracker.controller;

import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.CoinBalanceResponse;
import com.goaltracker.dto.RewardRequest;
import com.goaltracker.dto.RewardResponse;
import com.goaltracker.service.RewardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    // GET /api/rewards -> 獎勵清單(附帶目前餘額夠不夠兌換的旗標)
    @GetMapping
    public ApiResponse<List<RewardResponse>> getAll() {
        return ApiResponse.ok(rewardService.getAll());
    }

    // POST /api/rewards -> 新增自訂獎勵項目
    @PostMapping
    public ApiResponse<RewardResponse> create(@Valid @RequestBody RewardRequest request) {
        return ApiResponse.ok("獎勵已新增", rewardService.create(request));
    }

    // DELETE /api/rewards/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        rewardService.delete(id);
        return ApiResponse.ok("獎勵已刪除", null);
    }

    // POST /api/rewards/{id}/redeem -> 兌換(金幣不夠會被擋下來)
    @PostMapping("/{id}/redeem")
    public ApiResponse<CoinBalanceResponse> redeem(@PathVariable Long id) {
        return ApiResponse.ok("兌換成功", rewardService.redeem(id));
    }
}
