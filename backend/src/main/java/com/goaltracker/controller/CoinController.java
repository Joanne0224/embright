package com.goaltracker.controller;

import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.CoinBalanceResponse;
import com.goaltracker.dto.CoinTransactionResponse;
import com.goaltracker.service.CoinService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
public class CoinController {

    private final CoinService coinService;

    public CoinController(CoinService coinService) {
        this.coinService = coinService;
    }

    // GET /api/coins/balance -> 目前金幣餘額
    @GetMapping("/balance")
    public ApiResponse<CoinBalanceResponse> getBalance() {
        return ApiResponse.ok(coinService.getBalance());
    }

    // GET /api/coins/transactions -> 金幣進出歷史(賺/花都在這裡)
    @GetMapping("/transactions")
    public ApiResponse<List<CoinTransactionResponse>> getHistory() {
        return ApiResponse.ok(coinService.getHistory());
    }

    // POST /api/coins/checkin -> 每日簽到,一天限一次
    @PostMapping("/checkin")
    public ApiResponse<CoinBalanceResponse> checkin() {
        return ApiResponse.ok("簽到成功,+10 金幣", coinService.checkin());
    }
}
