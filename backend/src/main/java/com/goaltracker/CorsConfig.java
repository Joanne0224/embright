package com.goaltracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨網域設定(CORS)
 *
 * 為什麼需要這個:
 * 前端(部署在 GitHub Pages,網域是 xxx.github.io)跟後端(部署在 Railway,網域是 xxx.up.railway.app)
 * 是「不同網域」,瀏覽器基於安全考量,預設會擋掉不同網域之間的 API 請求(這就是 CORS 政策)。
 * 如果沒有這段設定,前端呼叫 API 時瀏覽器 console 會出現紅字的 CORS 錯誤,資料完全叫不回來。
 *
 * allowed-origins 從 application.properties 讀取,本機開發跟正式部署可以用不同網址,
 * 不用改程式碼、只要改設定檔或環境變數。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
