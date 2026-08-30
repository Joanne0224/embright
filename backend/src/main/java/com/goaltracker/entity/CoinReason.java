package com.goaltracker.entity;

/**
 * 金幣進出的原因分類。
 * CHECKIN / TASK_COMPLETE / GOAL_COMPLETE 都是「賺」(amount 為正數),
 * REWARD_REDEEM 是「花」(amount 為負數)——用同一張表記錄所有進出,
 * 餘額用 SUM(amount) 算出來,而不是另外存一個會跟紀錄兜不起來的「餘額」欄位。
 */
public enum CoinReason {
    CHECKIN,        // 每日簽到
    TASK_COMPLETE,  // 完成任務
    GOAL_COMPLETE,  // 完成目標(雙倍獎勵)
    REWARD_REDEEM   // 兌換獎勵(扣款)
}
