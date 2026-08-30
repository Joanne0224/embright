package com.goaltracker.exception;

import com.goaltracker.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全域例外處理攔截器(@RestControllerAdvice)
 *
 * 這個設計呼應第37堂的課堂討論——同學郭峯銘當時問老師:「網站爆炸的問題,
 * 可以用 @ControllerAdvice 做全域例外處理攔截嗎?」答案是可以,而且是業界標準做法。
 *
 * 為什麼要集中處理例外、不要每個 Controller 方法自己包 try-catch?
 * - 不用每支 API 都重複寫一樣的錯誤處理邏輯,錯誤格式才能真正統一
 * - 業務邏輯(Service層丟例外)跟「怎麼把例外轉成HTTP回應」這兩件事分開,職責更清楚
 * - 新增一種例外類型時,只要在這裡加一個 @ExceptionHandler,全站馬上套用,不用逐一修改
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 資源找不到 → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    // 業務邏輯驗證失敗(例如:短期目標的 parentId 指向的不是中期目標)→ 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    // DTO 上的 @NotBlank / @NotNull 驗證沒過 → 400,把第一個錯誤訊息取出來
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("輸入資料格式不正確");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    // 資料庫層的 FK/唯一鍵限制擋下操作(例如如果之後改成用DB外鍵約束)→ 400,給友善訊息而非原始SQL錯誤
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("這個操作違反資料庫的關聯限制,請確認關聯的資料是否還存在"));
    }

    // 其他所有沒被上面攔到的例外 → 500,避免把內部錯誤細節(例如SQL訊息)直接曝露給前端
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("伺服器發生錯誤,請稍後再試"));
    }
}
