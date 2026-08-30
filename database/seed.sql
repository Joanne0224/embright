-- ============================================================
-- 範例資料:讓第一次啟動系統時,畫面不是空的,方便你自己先測試、也方便老師直接看到成果
-- 使用方式:先執行 schema.sql 建表,再執行這份 seed.sql
-- 用 session 變數接住 LAST_INSERT_ID(),避免硬寫死 id 出錯
-- ============================================================

USE railway;

-- ---------- 面向:工作 ----------
INSERT INTO domains (name, color, sort_order) VALUES ('工作', 'accent', 0);
SET @domain_work = LAST_INSERT_ID();

INSERT INTO goals (domain_id, parent_id, type, title, description, target_date, status)
VALUES (@domain_work, NULL, 'LONG', '三個月內考到證照', '轉職前累積技術實力的第一步', DATE_ADD(CURDATE(), INTERVAL 3 MONTH), 'IN_PROGRESS');
SET @goal_long_work = LAST_INSERT_ID();

INSERT INTO goals (domain_id, parent_id, type, title, description, target_date, status)
VALUES (@domain_work, @goal_long_work, 'MID', '第一個月完成教材', '把指定範圍的教材全部看過一輪', DATE_ADD(CURDATE(), INTERVAL 1 MONTH), 'IN_PROGRESS');
SET @goal_mid_work = LAST_INSERT_ID();

INSERT INTO goals (domain_id, parent_id, type, title, description, target_date, status)
VALUES (@domain_work, @goal_mid_work, 'SHORT', '本週讀完第三章', NULL, DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS');
SET @goal_short_work = LAST_INSERT_ID();

INSERT INTO tasks (goal_id, title, frequency, task_date, completed, completed_at)
VALUES (@goal_short_work, '讀第3章第1節', 'DAILY', CURDATE(), TRUE, NOW());
SET @task_done_work = LAST_INSERT_ID();

INSERT INTO tasks (goal_id, title, frequency, task_date, completed)
VALUES (@goal_short_work, '做10題練習題', 'DAILY', CURDATE(), FALSE);

INSERT INTO completion_logs (task_id, goal_id, domain_id, completed_date)
VALUES (@task_done_work, @goal_short_work, @domain_work, CURDATE());

-- ---------- 面向:學習 ----------
INSERT INTO domains (name, color, sort_order) VALUES ('學習', 'pro', 1);
SET @domain_study = LAST_INSERT_ID();

INSERT INTO goals (domain_id, parent_id, type, title, description, target_date, status)
VALUES (@domain_study, NULL, 'LONG', '學會全端開發', 'HTML/CSS/JS + Spring Boot + MySQL', DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 'IN_PROGRESS');
SET @goal_long_study = LAST_INSERT_ID();

INSERT INTO goals (domain_id, parent_id, type, title, description, target_date, status)
VALUES (@domain_study, @goal_long_study, 'MID', '完成進階Java專案課', NULL, DATE_ADD(CURDATE(), INTERVAL 2 MONTH), 'IN_PROGRESS');
SET @goal_mid_study = LAST_INSERT_ID();

INSERT INTO goals (domain_id, parent_id, type, title, description, target_date, status)
VALUES (@domain_study, @goal_mid_study, 'SHORT', '完成目標整理器作業', '週三前交出可跑的成品', CURDATE(), 'IN_PROGRESS');
SET @goal_short_study = LAST_INSERT_ID();

INSERT INTO tasks (goal_id, title, frequency, task_date, completed)
VALUES (@goal_short_study, '寫完後端 API', 'ONE_TIME', CURDATE(), FALSE);

INSERT INTO tasks (goal_id, title, frequency, task_date, completed)
VALUES (@goal_short_study, '推上 GitHub 並部署', 'ONE_TIME', CURDATE(), FALSE);

-- ---------- 面向:家庭、生活(先建立空的,示範多面向卡片並排的樣子)----------
INSERT INTO domains (name, color, sort_order) VALUES ('家庭', 'success', 2);
INSERT INTO domains (name, color, sort_order) VALUES ('生活', 'warning', 3);

-- ---------- 金幣系統範例資料 ----------
-- 呼應上面已經完成的那筆任務(task_done_work),補一筆對應的金幣紀錄,讓餘額跟紀錄對得起來
INSERT INTO coin_transactions (amount, reason, reference_id, note)
VALUES (5, 'TASK_COMPLETE', @task_done_work, '完成任務');

INSERT INTO coin_transactions (amount, reason, reference_id, note)
VALUES (10, 'CHECKIN', NULL, '每日簽到');

-- 幾個自訂獎勵範例,讓「獎勵中心」分頁一開始不是空的
INSERT INTO rewards (title, cost) VALUES ('追一集喜歡的劇', 30);
INSERT INTO rewards (title, cost) VALUES ('買一杯手搖飲', 50);
INSERT INTO rewards (title, cost) VALUES ('耍廢半小時不內疚', 20);
