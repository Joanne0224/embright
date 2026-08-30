package com.goaltracker.auth;

/**
 * 「目前是誰在操作」的暫存區,用 ThreadLocal 實作。
 *
 * 為什麼需要這個:每個 HTTP 請求進來時,Java Web Server 會分配一個執行緒(thread)去處理它。
 * AuthFilter 會在請求最前面,把「這個請求是哪個使用者」解析出來,存進這個 ThreadLocal;
 * 後面的 Controller、Service 不用每個方法都多一個 userId 參數,直接呼叫
 * CurrentUserHolder.getUserId() 就能拿到答案——因為它們是在「同一個執行緒」裡執行的。
 *
 * 請求處理完一定要呼叫 clear(),不然下一個剛好用到同一條執行緒的請求,
 * 可能會意外拿到上一個人的使用者 id(這是 ThreadLocal 常見的資安陷阱,要小心處理)。
 */
public class CurrentUserHolder {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    public static void set(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
