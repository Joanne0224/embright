package com.goaltracker.repository;

import com.goaltracker.entity.Goal;
import com.goaltracker.entity.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    // 查某個面向底下,指定層級(長/中/短)的目標——目標地圖頁的長期目標列表就是這樣查
    List<Goal> findByDomainIdAndType(Long domainId, GoalType type);

    // 查某個目標底下的直屬子目標(長期→找中期,中期→找短期)
    List<Goal> findByParentId(Long parentId);

    // 計算子目標數量,給 GoalResponse 的 childCount 用
    long countByParentId(Long parentId);

    // 依面向查出所有層級的目標,用來統計面向卡片上的完成度
    List<Goal> findByDomainId(Long domainId);
}
