package com.goaltracker.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增/編輯面向時,前端要送過來的資料格式。
 *
 * 為什麼要另外寫 DTO、不直接把 Entity 拿來當作 API 的輸入/輸出?(呼應第5、10堂筆記的 DTO 契約設計)
 * - Entity 是資料庫的鏡子,欄位會隨著資料庫設計調整(例如以後加審計欄位);
 *   DTO 是前後端之間的「契約」,欄位由「這支 API 需要什麼」決定,兩者關注點不同,容易走鐘
 * - 用 Entity 當輸入,前端可以亂塞 id、createdAt 這種不該由使用者決定的欄位,是資安風險
 * - DTO 可以加驗證規則(@NotBlank),Entity 上加太多驗證註解會讓資料庫層邏輯變得雜亂
 */
public record DomainRequest(
        @NotBlank(message = "面向名稱不能是空的")
        String name,

        String color,

        Integer sortOrder
) {
}
