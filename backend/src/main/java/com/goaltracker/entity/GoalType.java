package com.goaltracker.entity;

/**
 * 目標層級:長期 / 中期 / 短期
 * 這三種目標其實存在同一張 goals 表裡,靠這個欄位 + parent_id 來區分層級關係。
 */
public enum GoalType {
    LONG,   // 長期目標,parent_id 一定是 null(頂層)
    MID,    // 中期目標,parent_id 指向一個 LONG 目標
    SHORT   // 短期目標,parent_id 指向一個 MID 目標
}
