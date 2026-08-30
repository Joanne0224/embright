package com.goaltracker.auth;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 極簡的登入權杖(token)管理——登入成功後發一組隨機字串當「通行證」,
 * 之後每次呼叫 API 都要附帶這組字串,伺服器查表就知道「這是哪個使用者」。
 *
 * 為什麼不用更正式的 JWT 或 Spring Security Session:
 * 這是個人使用規模的小系統,不是要撐大量使用者的正式產品。
 * 用一個 Map 存在記憶體裡,做法簡單、容易懂、也容易在筆記裡講清楚原理——
 * 這正好符合「先求正確可用,不追求過度複雜」的 MVP 精神。
 *
 * 這個做法的已知限制(誠實面對取捨,不是沒想到):
 * 伺服器重新啟動(例如 Railway 重新部署)時,記憶體會被清空,所有人都要重新登入一次。
 * 未來如果要正式上線給很多人用,會需要換成 JWT 或者把 token 存進資料庫。
 */
@Service
public class TokenService {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String issueToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Long resolveUserId(String token) {
        if (token == null) return null;
        return tokenToUserId.get(token);
    }

    public void revoke(String token) {
        tokenToUserId.remove(token);
    }
}
