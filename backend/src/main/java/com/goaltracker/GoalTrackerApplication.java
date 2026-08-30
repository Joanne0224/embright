package com.goaltracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * 目標整理器 - 主程式進入點
 *
 * 為什麼需要這個類別:
 * Spring Boot 專案一定要有一個帶 @SpringBootApplication 的類別當作啟動點。
 * 這個註解其實是三個註解的組合包(@Configuration + @EnableAutoConfiguration + @ComponentScan),
 * 它會自動掃描 com.goaltracker 底下所有的 @Controller、@Service、@Repository、@Entity,
 * 不用你一個一個手動註冊——這就是第4堂筆記提到的「框架化簡重複工作」。
 */
@SpringBootApplication
public class GoalTrackerApplication {

    public static void main(String[] args) {
        // 這行一定要在 Spring Boot 啟動之前設定:把整個程式的「現在幾點、今天幾號」都鎖定成台灣時區。
        // 沒設定的話,Railway 這種雲端伺服器預設會用 UTC(格林威治時間),
        // 比台灣慢 8 小時——凌晨 0 點到早上 8 點之間,伺服器認定的「今天」會跟你錯開一天,
        // 「今日任務」「今日簽到」這種依賴「今天日期」的功能就會抓錯日期。
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        SpringApplication.run(GoalTrackerApplication.class, args);
    }
}
