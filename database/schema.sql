-- ============================================================
-- 目標整理器 資料庫建表語法
-- 使用方式:mysql -u root -p < schema.sql
-- (或用 MySQL Workbench / DBeaver 開啟這個檔案直接執行)
--
-- 這份 schema.sql 是「正式版」的資料庫結構,包含完整的 FK 約束跟索引。
-- 本機開發時 Spring Boot 用 ddl-auto=update 也能自動建表(比較快、不用手動跑這份檔案),
-- 但那個模式不會建立真正的 FK 約束。想要展示「PK/FK 關聯設計」給老師看,
-- 或是正式部署到 Railway 時,建議先執行這份 schema.sql,
-- 並把 application.properties 的 ddl-auto 改成 validate。
-- ============================================================

CREATE DATABASE IF NOT EXISTS goal_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE goal_tracker;

-- ------------------------------------------------------------
-- 1. domains:面向(工作/學習/家庭/生活...)—— 整個階層的最上層
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS domains (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    color       VARCHAR(20)  DEFAULT 'blue',
    sort_order  INT          DEFAULT 0,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 2. goals:長/中/短期目標,parent_id 自關聯自己形成三層樹狀結構
--    長期目標 parent_id = NULL;中期指向長期;短期指向中期
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS goals (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    domain_id     BIGINT       NOT NULL,
    parent_id     BIGINT       NULL,
    type          ENUM('LONG', 'MID', 'SHORT') NOT NULL,
    title         VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    target_date   DATE,
    status        ENUM('IN_PROGRESS', 'COMPLETED', 'ARCHIVED') NOT NULL DEFAULT 'IN_PROGRESS',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_goals_domain FOREIGN KEY (domain_id) REFERENCES domains(id) ON DELETE CASCADE,
    CONSTRAINT fk_goals_parent FOREIGN KEY (parent_id) REFERENCES goals(id) ON DELETE CASCADE,

    INDEX idx_goals_domain (domain_id),
    INDEX idx_goals_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 3. tasks:掛在短期目標下的每日/每週具體行動事項
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tasks (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id       BIGINT       NOT NULL,
    title         VARCHAR(100) NOT NULL,
    frequency     ENUM('DAILY', 'WEEKLY', 'ONE_TIME') NOT NULL,
    task_date     DATE         NOT NULL,
    completed     BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at  DATETIME     NULL,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tasks_goal FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE,

    INDEX idx_tasks_goal (goal_id),
    INDEX idx_tasks_date (task_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 4. completion_logs:完成歷史紀錄(永久保留,查詢用)
--    task_id / goal_id / domain_id 用 ON DELETE SET NULL,
--    確保就算對應的任務/目標/面向被刪除,這筆完成歷史依然存在,不會被連帶刪掉
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS completion_logs (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id          BIGINT       NULL,
    goal_id          BIGINT       NULL,
    domain_id        BIGINT       NULL,
    completed_date   DATE         NOT NULL,
    note             VARCHAR(200),
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_logs_task   FOREIGN KEY (task_id)   REFERENCES tasks(id)   ON DELETE SET NULL,
    CONSTRAINT fk_logs_goal   FOREIGN KEY (goal_id)   REFERENCES goals(id)   ON DELETE SET NULL,
    CONSTRAINT fk_logs_domain FOREIGN KEY (domain_id) REFERENCES domains(id) ON DELETE SET NULL,

    INDEX idx_logs_date (completed_date),
    INDEX idx_logs_domain (domain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 5. coin_transactions:金幣進出紀錄(簽到/完成任務/完成目標/兌換獎勵)
--    「目前餘額」不存欄位,用 SUM(amount) 算出來——這樣紀錄跟餘額永遠對得起來,
--    不會發生「餘額欄位」跟「交易紀錄」兜不合的資料不一致問題
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS coin_transactions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount         INT          NOT NULL,   -- 正數=賺到,負數=花掉
    reason         ENUM('CHECKIN', 'TASK_COMPLETE', 'GOAL_COMPLETE', 'REWARD_REDEEM') NOT NULL,
    reference_id   BIGINT       NULL,       -- 對應的任務/目標/獎勵 id,不強制設 FK(這三種來源表不同,用單一FK無法表達,交由應用層維護)
    note           VARCHAR(200),
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_coin_tx_reason (reason),
    INDEX idx_coin_tx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 6. rewards:自訂獎勵項目(存多少金幣可以兌換什麼)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rewards (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(100) NOT NULL,
    cost         INT          NOT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
