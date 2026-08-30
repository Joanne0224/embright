package com.goaltracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 面向(Domain):工作 / 學習 / 家庭 / 生活...
 *
 * 這是整個目標階層的「最上層容器」,每個面向底下才會掛長期目標。
 * color / sortOrder 是為了支援首頁的卡片式版面(不同面向不同顏色、可以調整排列順序)。
 *
 * 注意:這個類別沒有用 Lombok(@Getter/@Setter),getter/setter 都是手寫的。
 * 原因是 Lombok 在新版 JDK(23以上)需要額外設定才能運作,為了避免環境問題,
 * 這個專案改成全部手寫,雖然多一點程式碼,但每一行都看得到、不用猜。
 */
@Entity
@Table(name = "domains")
public class Domain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 這個面向屬於哪個使用者——有了登入系統之後,每個人只會看到自己的面向
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    // 卡片顏色標籤,例如 "blue" "purple" "green" "amber",前端依這個值套色
    @Column(length = 20)
    private String color = "blue";

    // 排序用,數字越小排越前面。使用上下箭頭調整順序時,就是在改這個值。
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
