package com.goaltracker.repository;

import com.goaltracker.entity.CompletionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletionLogRepository extends JpaRepository<CompletionLog, Long> {

    // 完成紀錄頁:全部歷史,新的在前面
    List<CompletionLog> findAllByOrderByCompletedDateDesc();

    // 查某個面向的完成歷史(之後想做「這個面向本月完成幾次」統計會用到)
    List<CompletionLog> findByDomainIdOrderByCompletedDateDesc(Long domainId);

    // 查某個目標的完成歷史
    List<CompletionLog> findByGoalIdOrderByCompletedDateDesc(Long goalId);

    // 查某個任務的完成紀錄(取消完成時,要把最近一筆刪掉,讓「完成/取消」這兩個動作互相對稱)
    List<CompletionLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
