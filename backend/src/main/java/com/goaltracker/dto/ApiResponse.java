package com.goaltracker.dto;

/**
 * 統一的 API 回應格式:{ success, message, data }
 * 讓前端不管呼叫哪一支 API,都用同一套邏輯判斷成功/失敗、拿資料——這是老師在第38堂
 * 「API設計文件」裡建議的通用回應格式,前端寫起來也比較不會每支 API 都要處理不同格式。
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "success", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
