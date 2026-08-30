package com.goaltracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        SpringApplication.run(GoalTrackerApplication.class, args);
    }
}
