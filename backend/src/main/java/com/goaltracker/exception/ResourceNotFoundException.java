package com.goaltracker.exception;

/**
 * 找不到資源時丟出的例外(例如查一個不存在的目標 id)。
 * 搭配 GlobalExceptionHandler,會自動轉成 404 + 統一格式的錯誤訊息回給前端。
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
