package com.goaltracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 使用者帳號。
 *
 * passwordHash 存的是加密過的密碼,不是明文——這是資安基本要求,即使是自己一個人用的小系統也一樣。
 * 這裡用 BCrypt 演算法加密(在 AuthService 做),BCrypt 的特色是「單向」的:
 * 只能拿明文密碼去驗證雜湊值對不對,沒辦法反過來從雜湊值還原出原始密碼——
 * 就算資料庫外洩,攻擊者拿到的也只是雜湊值,不是你的真實密碼。
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
