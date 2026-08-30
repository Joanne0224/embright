package com.goaltracker.service;

import com.goaltracker.dto.CompletionLogResponse;
import com.goaltracker.entity.CompletionLog;
import com.goaltracker.entity.Domain;
import com.goaltracker.entity.Goal;
import com.goaltracker.entity.Task;
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
 * 如果用資料庫 JOIN,被刪除的資料會直接讓那一整列消失或報錯,不符合「永久紀錄」的設計初衷。
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
                .map(this::toResponse)
                .toList();
    }

    public List<CompletionLogResponse> getByDomain(Long domainId) {
        return completionLogRepository.findByDomainIdOrderByCompletedDateDesc(domainId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CompletionLogResponse> getByGoal(Long goalId) {
        return completionLogRepository.findByGoalIdOrderByCompletedDateDesc(goalId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CompletionLogResponse toResponse(CompletionLog log) {
        String taskTitle = taskRepository.findById(log.getTaskId())
                .map(Task::getTitle)
                .orElse("(已刪除)");
        String goalTitle = goalRepository.findById(log.getGoalId())
                .map(Goal::getTitle)
                .orElse("(已刪除)");
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
