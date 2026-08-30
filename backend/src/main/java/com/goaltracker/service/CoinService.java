package com.goaltracker.service;

import com.goaltracker.auth.CurrentUserHolder;
import com.goaltracker.dto.CoinBalanceResponse;
import com.goaltracker.dto.CoinTransactionResponse;
import com.goaltracker.entity.CoinReason;
import com.goaltracker.entity.CoinTransaction;
import com.goaltracker.repository.CoinTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 金幣系統的核心服務。加了登入系統之後,每個人的金幣是分開算的——
 * 餘額查詢、簽到、發放獎勵,全部都會先看 CurrentUserHolder.getUserId() 是誰。
 */
@Service
public class CoinService {

    private static final int CHECKIN_REWARD = 10;
    private static final int TASK_COMPLETE_REWARD = 5;
    private static final int GOAL_COMPLETE_REWARD = 20; // 基礎值10 x 雙倍

    private final CoinTransactionRepository coinTransactionRepository;

    public CoinService(CoinTransactionRepository coinTransactionRepository) {
        this.coinTransactionRepository = coinTransactionRepository;
    }

    public CoinBalanceResponse getBalance() {
        return new CoinBalanceResponse(coinTransactionRepository.getBalance(CurrentUserHolder.getUserId()));
    }

    public List<CoinTransactionResponse> getHistory() {
        return coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(CurrentUserHolder.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 每日簽到:一天只能簽一次(依使用者分開計算)
    public CoinBalanceResponse checkin() {
        Long userId = CurrentUserHolder.getUserId();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        boolean alreadyCheckedIn = coinTransactionRepository.existsByUserIdAndReasonAndCreatedAtBetween(
                userId, CoinReason.CHECKIN, startOfToday, startOfTomorrow);
        if (alreadyCheckedIn) {
            throw new IllegalArgumentException("今天已經簽到過了,明天再來吧");
        }

        award(CHECKIN_REWARD, CoinReason.CHECKIN, null, "每日簽到");
        return getBalance();
    }

    // 完成任務時呼叫(由 TaskService 在打勾完成時觸發)
    public void awardTaskComplete(Long taskId) {
        award(TASK_COMPLETE_REWARD, CoinReason.TASK_COMPLETE, taskId, "完成任務");
    }

    // 取消完成任務時呼叫——把剛剛發的金幣收回來,讓「完成/取消」這兩個動作互相對稱
    public void reverseTaskComplete(Long taskId) {
        award(-TASK_COMPLETE_REWARD, CoinReason.TASK_COMPLETE, taskId, "取消完成任務(收回獎勵)");
    }

    // 完成目標時呼叫(由 GoalService 在標記達成時觸發)——雙倍獎勵
    public void awardGoalComplete(Long goalId) {
        award(GOAL_COMPLETE_REWARD, CoinReason.GOAL_COMPLETE, goalId, "完成目標(雙倍獎勵)");
    }

    // 兌換獎勵:扣款(由 RewardService 呼叫,amount 傳負數)
    public void spend(int amount, Long rewardId, String note) {
        award(-Math.abs(amount), CoinReason.REWARD_REDEEM, rewardId, note);
    }

    private void award(int amount, CoinReason reason, Long referenceId, String note) {
        CoinTransaction tx = new CoinTransaction();
        tx.setUserId(CurrentUserHolder.getUserId());
        tx.setAmount(amount);
        tx.setReason(reason);
        tx.setReferenceId(referenceId);
        tx.setNote(note);
        coinTransactionRepository.save(tx);
    }

    private CoinTransactionResponse toResponse(CoinTransaction tx) {
        return new CoinTransactionResponse(tx.getId(), tx.getAmount(), tx.getReason(), tx.getNote(), tx.getCreatedAt());
    }
}
