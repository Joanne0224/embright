package com.goaltracker.service;

import com.goaltracker.auth.CurrentUserHolder;
import com.goaltracker.dto.CompletionLogResponse;
import com.goaltracker.entity.CompletionLog;
import com.goaltracker.entity.Domain;
import com.goaltracker.entity.Goal;
import com.goaltracker.entity.Task;
import com.goaltracker.exception.ResourceNotFoundException;
import com.goaltracker.repository.CompletionLogRepository;
import com.goaltracker.repository.DomainRepository;
import com.goaltracker.repository.GoalRepository;
import com.goaltracker.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 完成紀錄查詢——這支對應老師會當場測試的「查詢功能」。
 *
 * 這裡刻意用「查完 id 再去查標題」的方式(而不是資料庫 JOIN),
 * 是因為 completion_logs 是永久保留的歷史紀錄,即使對應的 task/goal 之後被刪除了,
 * 這筆歷史還是要查得到、不能報錯——只是標題會顯示「(已刪除)」。
 *
 * 資安提醒:每一筆紀錄都要先確認它的 domain_id 屬於目前登入的使用者才回傳,
 * 找不到對應面向(可能被刪除、或不是自己的)的紀錄,安全起見一律不顯示,而不是照樣秀出來。
 */
@Service
public class CompletionLogService {

    private final CompletionLogRepository completionLogRepository;
    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final DomainRepository domainRepository;

    public CompletionLogService(CompletionLogRepository completionLogRepository, TaskRepository taskRepository,
                                 GoalRepository goalRepository, DomainRepository domainRepository) {
        this.completionLogRepository = completionLogRepository;
        this.taskRepository = taskRepository;
        this.goalRepository = goalRepository;
        this.domainRepository = domainRepository;
    }

    public List<CompletionLogResponse> getAll() {
        return completionLogRepository.findAllByOrderByCompletedDateDesc()
                .stream()
                .filter(this::isOwnedByCurrentUser)
                .map(this::toResponse)
                .toList();
    }

    public List<CompletionLogResponse> getByDomain(Long domainId) {
        assertDomainOwned(domainId);
        return completionLogRepository.findByDomainIdOrderByCompletedDateDesc(domainId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CompletionLogResponse> getByGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + goalId + " 的目標"));
        assertDomainOwned(goal.getDomainId());
        return completionLogRepository.findByGoalIdOrderByCompletedDateDesc(goalId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void assertDomainOwned(Long domainId) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + domainId + " 的面向"));
        if (!domain.getUserId().equals(CurrentUserHolder.getUserId())) {
            throw new ResourceNotFoundException("找不到 id=" + domainId + " 的面向");
        }
    }

    // 給 getAll() 逐筆過濾用:domain 找不到或不是自己的,一律當作不屬於自己,安全起見不顯示
    private boolean isOwnedByCurrentUser(CompletionLog log) {
        if (log.getDomainId() == null) return false;
        Domain domain = domainRepository.findById(log.getDomainId()).orElse(null);
        return domain != null && domain.getUserId().equals(CurrentUserHolder.getUserId());
    }

    private CompletionLogResponse toResponse(CompletionLog log) {
        String goalTitle = goalRepository.findById(log.getGoalId())
                .map(Goal::getTitle)
                .orElse("(已刪除)");

        // taskId 是空的,代表這筆是「整個目標達成」的紀錄,不是某個任務完成的紀錄。
        // 這裡改成顯示一個短橫線,不重複填目標的名字——目標名字已經在「目標」那一欄看得到了,
        // 前端會另外用「類型」欄位的標籤,清楚區分這筆是任務完成還是目標達成
        String taskTitle = log.getTaskId() == null
                ? "－"
                : taskRepository.findById(log.getTaskId()).map(Task::getTitle).orElse("(已刪除)");
        String domainName = domainRepository.findById(log.getDomainId())
                .map(Domain::getName)
                .orElse("(已刪除)");

        return new CompletionLogResponse(
                log.getId(),
                log.getTaskId(),
                taskTitle,
                log.getGoalId(),
                goalTitle,
                log.getDomainId(),
                domainName,
                log.getCompletedDate(),
                log.getNote()
        );
    }
}
