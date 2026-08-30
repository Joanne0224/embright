package com.goaltracker.repository;

import com.goaltracker.entity.CoinReason;
import com.goaltracker.entity.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {

    List<CoinTransaction> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 餘額 = 這個使用者所有交易金額加總(正負相抵)。COALESCE 是為了一筆紀錄都沒有時,回傳 0 而不是 null。
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CoinTransaction c WHERE c.userId = :userId")
    long getBalance(@Param("userId") Long userId);

    // 檢查這個使用者今天是不是已經簽到過了(reason=CHECKIN 且 created_at 落在今天範圍內)
    boolean existsByUserIdAndReasonAndCreatedAtBetween(Long userId, CoinReason reason, LocalDateTime start, LocalDateTime end);
}
