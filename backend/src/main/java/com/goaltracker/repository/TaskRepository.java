package com.goaltracker.repository;

import com.goaltracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 查某個短期目標底下的所有任務
    List<Task> findByGoalId(Long goalId);

    // 「今日任務」頁面:查指定日期、跨所有面向的任務
    List<Task> findByTaskDateOrderByCompletedAsc(LocalDate taskDate);

    // 統計某個目標底下,已完成的任務數(給面向卡片的完成度進度條用)
    long countByGoalIdAndCompletedTrue(Long goalId);

    long countByGoalId(Long goalId);
}
