package com.goaltracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 金幣交易紀錄——每一次賺/花金幣,都在這裡留一筆事件紀錄。
 * 這個類別沒有用 Lombok,getter/setter 都是手寫的(原因見 Domain.java 的註解)。
 */
@Entity
@Table(name = "coin_transactions")
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 正數 = 賺到的金幣;負數 = 花掉的金幣
    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoinReason reason;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(length = 200)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public CoinReason getReason() { return reason; }
    public void setReason(CoinReason reason) { this.reason = reason; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
