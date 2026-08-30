package com.goaltracker.service;

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
 * 金幣系統的核心服務。
 *
 * award() 是唯一「寫入」金幣紀錄的入口——不管是簽到、完成任務、完成目標、還是兌換獎勵(負數),
 * 全部都要經過這個方法。集中成單一入口的好處:以後如果要加「防止重複發放」之類的規則,
 * 只要改這一個地方,不用擔心某個呼叫端漏改。
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
        return new CoinBalanceResponse(coinTransactionRepository.getBalance());
    }

    public List<CoinTransactionResponse> getHistory() {
        return coinTransactionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 每日簽到:一天只能簽一次
    public CoinBalanceResponse checkin() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        boolean alreadyCheckedIn = coinTransactionRepository.existsByReasonAndCreatedAtBetween(
                CoinReason.CHECKIN, startOfToday, startOfTomorrow);
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

    // 取消完成任務時呼叫——把剛剛發的金幣收回來,讓「完成/取消」這兩個動作互相對稱,
    // 不然使用者可以靠「打勾→取消→再打勾」無限刷金幣(這是你自己測出來的那個 bug)
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
