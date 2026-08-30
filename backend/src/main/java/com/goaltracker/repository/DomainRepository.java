package com.goaltracker.repository;

import com.goaltracker.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    // 依排序值由小到大取出,首頁卡片就是照這個順序排列(只查屬於這個使用者的)
    List<Domain> findAllByUserIdOrderBySortOrderAsc(Long userId);
}
