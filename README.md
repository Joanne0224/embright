# 築光 Embright

多面向(工作/學習/家庭/生活...)的長中短期目標管理系統。每個面向底下可以建立長期目標 → 中期目標 → 短期目標,短期目標再拆解成每日/每週任務,完成的任務會永久記錄在完成歷史裡,可以查詢。每天簽到、完成任務/目標都能累積金幣,兌換自己設定的獎勵——把每天完成的小事,一點一點堆成想要的光。

技術架構:HTML/CSS/JS 前端 + Spring Boot 後端(三層式架構)+ MySQL 資料庫。

---

## 一、專案結構

```
embright/
├── backend/              Spring Boot 後端專案
│   ├── pom.xml
│   └── src/main/java/com/goaltracker/
│       ├── entity/        6張表對應的 Entity(Domain, Goal, Task, CompletionLog, CoinTransaction, Reward)+ 4個enum
│       ├── dto/            API 輸入/輸出的資料格式(契約設計)
│       ├── repository/     Spring Data JPA 資料庫存取層
│       ├── service/        商業邏輯層(驗證規則、跨表查詢都在這裡)
│       ├── controller/     REST API 進入點
│       └── exception/      全域例外處理(@RestControllerAdvice)
├── database/
│   ├── schema.sql          正式建表語法(含完整 FK 約束,給老師看 ER 設計用)
│   └── seed.sql             範例資料,讓系統一開始不是空的
├── frontend/
│   └── index.html          單一頁面前端,4個分頁全部在這一個檔案裡
└── README.md               就是你現在在看的這份
```

---

## 二、本機開發:怎麼跑起來

### 1. 建資料庫(用你本機已裝好的 MySQL,對應第26堂筆記)

打開終端機或 MySQL Workbench,執行:

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

這樣會建立 `goal_tracker` 資料庫跟 6 張表,並塞入幾筆範例資料。

### 2. 設定資料庫密碼

`backend/src/main/resources/application.properties` 這個檔案**含有你的真實 MySQL 密碼,已經被 `.gitignore` 排除,不會被推上 GitHub**。第一次設定時:

1. 複製一份範例檔:把 `application.properties.example` 複製一份,改名成 `application.properties`(如果你照這份 README 一路做下來,這個檔案應該已經存在,不用重做)
2. 打開 `application.properties`,如果你的 MySQL root 密碼不是空的,把這一行改成你的密碼:
   ```properties
   spring.datasource.password=${DB_PASSWORD:你的MySQL密碼}
   ```

**這是為什麼**:`application.properties.example` 是「範本」,不含任何真密碼,可以安心進版控,讓其他人(或未來的你)知道要填哪些欄位;真正含有密碼的 `application.properties` 被 `.gitignore` 擋掉,不會被上傳。

### 3. 用 VS Code 打開 backend 資料夾,啟動後端

```bash
cd backend
mvn spring-boot:run
```

看到 `Started GoalTrackerApplication` 就代表啟動成功,後端網址是 `http://localhost:8080`。

### 4. 打開前端

用 VS Code 的 Live Server 套件打開 `frontend/index.html`(或直接用瀏覽器打開這個檔案也可以)。

如果瀏覽器 console 出現 CORS 錯誤,檢查 `application.properties` 裡的 `app.cors.allowed-origins` 有沒有包含你打開前端的網址(例如 Live Server 預設是 `http://127.0.0.1:5500`)。

---

## 三、快速測試流程(老師當場測試會走的路徑)

1. **儀表板**:看到「工作」「學習」「家庭」「生活」四張面向卡片(seed.sql 已經建好)
2. 點「+ 新增面向」→ 輸入名稱、選顏色 → 新增成功,馬上出現新卡片
3. 點進「工作」卡片 → 進入**目標地圖**,展開「三個月內考到證照」這個長期目標
4. 展開到「本週讀完第三章」這個短期目標,可以看到底下有任務、可以打勾
5. 打勾「做10題練習題」→ 切到**完成紀錄**分頁,應該能看到剛剛的完成紀錄出現在列表最上面
6. 切到**今日任務**分頁,可以看到跨所有面向、今天要做的任務彙整在一起

這條路徑走完,新增(POST)、查詢(GET)、更新(PATCH)三種操作都示範到了。

---

## 四、部署上線(對應老師建議的路徑)

### 前端 → GitHub Pages

1. 把整個 `embright` 資料夾推上 GitHub(見下方「推上 GitHub」)
2. GitHub repo 頁面 → Settings → Pages → Source 選 `main` branch,資料夾選 `/frontend`
3. 存檔後幾分鐘,前端網址會是 `https://你的帳號.github.io/repo名稱/`

### 後端 + 資料庫 → Railway

1. 到 [railway.app](https://railway.app) 用 GitHub 帳號登入
2. New Project → Deploy from GitHub repo → 選這個 repo,Root Directory 設成 `/backend`
3. 再新增一個 MySQL 服務(Railway 會自動產生連線資訊)
4. 到 Spring Boot 服務的 Variables 分頁,設定環境變數(從 MySQL 服務的連線資訊複製過來):
   - `DB_URL` = `jdbc:mysql://<Railway給的host>:<port>/<資料庫名稱>?useSSL=false&serverTimezone=Asia/Taipei`
   - `DB_USERNAME`、`DB_PASSWORD` = Railway MySQL 服務提供的帳密
   - `CORS_ORIGINS` = 你的 GitHub Pages 網址,例如 `https://你的帳號.github.io`
5. 部署成功後,把 `frontend/index.html` 裡的 `API_BASE` 改成 Railway 給的網址,例如:
   ```js
   const API_BASE = 'https://embright-production.up.railway.app/api';
   ```
6. 記得先用 Railway 的 MySQL 連線資訊執行一次 `database/schema.sql`,建好正式環境的資料表結構

### 推上 GitHub

```bash
cd embright
git init
git add .
git commit -m "築光 Embright MVP:多面向長中短期目標管理 + 金幣獎勵系統"
git branch -M main
git remote add origin https://github.com/你的帳號/embright.git
git push -u origin main
```

---

## 五、資料庫設計說明(對應老師要的「5個面向」之一:ER圖)

4張表,關聯方式:

- `domains`(面向)是最上層容器
- `goals`(目標)用 `parent_id` 自己關聯自己:長期目標的 `parent_id` 是 null,中期目標指向長期,短期目標指向中期——用一張表就能撐起三層樹狀結構,不用拆三張表
- `tasks`(任務)掛在短期目標底下,是每日/每週具體要做的事
- `completion_logs`(完成紀錄)是任務打勾完成時「額外」寫入的永久事件紀錄,即使任務或目標之後被刪除,這筆歷史依然查得到

詳細欄位跟 FK 約束,請看 `database/schema.sql` 裡的註解。

---

## 六、金幣系統(每日簽到 + 完成獎勵 + 自訂獎勵兌換)

再加 2 張表,共 6 張表:

- `coin_transactions`:金幣進出的事件紀錄,設計精神跟 `completion_logs` 一樣——目前餘額不是存出來的欄位,是把所有交易金額 `SUM()` 加總算出來的,永遠跟紀錄對得起來
- `rewards`:你自己定義的獎勵項目(名稱 + 所需金幣)

**賺金幣的規則**(寫在 `CoinService`,之後想調數字直接改那邊的常數就好):

| 動作 | 金幣 |
|---|---|
| 每日簽到(一天限一次) | +10 |
| 完成一個任務 | +5 |
| 完成一個目標(短/中/長期皆可) | +20(雙倍獎勵) |

**花金幣**:到「獎勵中心」分頁,自己新增獎勵項目(例如「追一集劇」設 30 枚),金幣夠了就能兌換,兌換會扣款並留下紀錄。金幣不夠時系統會擋下來,告訴你還差幾枚。

---

## 六、還沒做、如果時間允許可以再加的部分

- 面向卡片的真拖曳排序(目前是上下箭頭按鈕調順序,已符合「可自訂」需求,只是不是真拖曳)
- 編輯/刪除目標的 UI(後端 API 都已經寫好了,`PUT /api/goals/{id}`、`DELETE /api/goals/{id}`,只是前端還沒接編輯/刪除按鈕)
- 面向卡片的完成度統計圖表
