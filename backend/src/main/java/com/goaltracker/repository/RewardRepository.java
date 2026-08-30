package com.goaltracker.repository;

import com.goaltracker.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findAllByOrderByCostAsc();
}
