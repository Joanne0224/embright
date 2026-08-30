package com.goaltracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goaltracker.dto.ApiResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登入保護攔截器——所有 /api/** 的請求,除了 /api/auth/** (註冊、登入本身)之外,
 * 都要求附帶有效的 X-Auth-Token 標頭,否則直接擋下來回傳 401(未授權)。
 *
 * 為什麼不用完整的 spring-boot-starter-security:
 * 那個框架功能強大,但預設行為是「自動保護所有路徑」,設定複雜度高,
 * 對這個規模的專案來說殺雞用牛刀,還容易在設定過程中不小心把不該擋的路徑也擋住。
 * 這裡自己寫一個簡單的 Filter,邏輯完全掌握在自己手上,對現在的你來說也更容易看懂、講得出來。
 */
@Component
public class AuthFilter implements Filter {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();
        String method = request.getMethod();

        // CORS 預檢請求(瀏覽器自動發出的 OPTIONS)一定要放行,不然跨網域呼叫會整個失敗
        boolean isPreflight = "OPTIONS".equalsIgnoreCase(method);

        // 註冊、登入本身不需要 token(不然就變成要先登入才能登入,矛盾了);非 /api 的路徑也放行
        boolean isPublicPath = path.startsWith("/api/auth/") || !path.startsWith("/api/");

        if (isPreflight || isPublicPath) {
            chain.doFilter(req, res);
            return;
        }

        String token = request.getHeader("X-Auth-Token");
        Long userId = tokenService.resolveUserId(token);

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(ApiResponse.fail("尚未登入或登入已過期,請重新登入"))
            );
            return;
        }

        try {
            CurrentUserHolder.set(userId);
            chain.doFilter(req, res);
        } finally {
            // 一定要清掉,避免同一條執行緒下次處理別的請求時,誤用到這次的使用者身份
            CurrentUserHolder.clear();
        }
    }
}
