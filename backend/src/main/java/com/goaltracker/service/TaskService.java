package com.goaltracker.service;

import com.goaltracker.dto.TaskRequest;
import com.goaltracker.dto.TaskResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任務(Task)的商業邏輯層。
 *
 * 這裡最重要的方法是 complete():打勾完成任務的同時,要「同步寫入」completion_logs,
 * 這兩個動作必須綁在同一個交易(Transaction)裡——要嘛兩個都成功,要嘛兩個都失敗,
 * 不可以任務標記完成了、但歷史紀錄卻沒寫進去(資料會不一致)。這就是 @Transactional 的作用。
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final DomainRepository domainRepository;
    private final CompletionLogRepository completionLogRepository;
    private final CoinService coinService;

    public TaskService(TaskRepository taskRepository, GoalRepository goalRepository,
                        DomainRepository domainRepository, CompletionLogRepository completionLogRepository,
                        CoinService coinService) {
        this.taskRepository = taskRepository;
        this.goalRepository = goalRepository;
        this.domainRepository = domainRepository;
        this.completionLogRepository = completionLogRepository;
        this.coinService = coinService;
    }

    public List<TaskResponse> getByGoalId(Long goalId) {
        return taskRepository.findByGoalId(goalId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 「今日任務」頁面用:查指定日期、跨所有面向的任務,未完成的排前面
    public List<TaskResponse> getByDate(LocalDate date) {
        return taskRepository.findByTaskDateOrderByCompletedAsc(date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse create(TaskRequest request) {
        // 確認掛的目標真的存在,不能憑空掛一個不存在的 goalId
        goalRepository.findById(request.goalId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + request.goalId() + " 的目標"));

        Task task = new Task();
        task.setGoalId(request.goalId());
        task.setTitle(request.title());
        task.setFrequency(request.frequency());
        task.setTaskDate(request.taskDate());

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    // 打勾完成:同步更新 task 狀態 + 寫入 completion_logs 歷史紀錄
    @Transactional
    public TaskResponse complete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的任務"));

        if (Boolean.TRUE.equals(task.getCompleted())) {
            return toResponse(task); // 已經完成過了,直接回傳現況,不重複寫入歷史紀錄
        }

        task.setCompleted(true);
        task.setCompletedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        Goal goal = goalRepository.findById(task.getGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + task.getGoalId() + " 的目標"));

        CompletionLog log = new CompletionLog();
        log.setTaskId(task.getId());
        log.setGoalId(goal.getId());
        log.setDomainId(goal.getDomainId());
        log.setCompletedDate(LocalDate.now());
        completionLogRepository.save(log);

        // 完成任務同步發放金幣獎勵(呼應每日簽到金幣系統)
        coinService.awardTaskComplete(task.getId());

        return toResponse(saved);
    }

    // 取消完成(誤按可以復原)——這個動作要跟 complete() 對稱:
    // complete() 做了「+5金幣、寫一筆完成紀錄」,uncomplete() 就要「收回金幣、刪掉那筆紀錄」,
    // 不然「打勾→取消→再打勾」可以無限刷金幣(這正是你剛剛測出來的 bug)
    @Transactional
    public TaskResponse uncomplete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的任務"));

        if (!Boolean.TRUE.equals(task.getCompleted())) {
            return toResponse(task); // 本來就還沒完成,不用做任何事,避免重複收回金幣
        }

        task.setCompleted(false);
        task.setCompletedAt(null);
        Task saved = taskRepository.save(task);

        // 把這個任務最近一次的完成紀錄刪掉(對應剛剛那次 complete() 寫入的那一筆)
        List<CompletionLog> logs = completionLogRepository.findByTaskIdOrderByCreatedAtDesc(task.getId());
        if (!logs.isEmpty()) {
            completionLogRepository.delete(logs.get(0));
        }

        coinService.reverseTaskComplete(task.getId());

        return toResponse(saved);
    }

    public void delete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的任務"));
        taskRepository.delete(task);
    }

    private TaskResponse toResponse(Task task) {
        String goalTitle = "(已刪除)";
        String domainName = "(已刪除)";
        Long domainId = null;

        Goal goal = goalRepository.findById(task.getGoalId()).orElse(null);
        if (goal != null) {
            goalTitle = goal.getTitle();
            domainId = goal.getDomainId();
            Domain domain = domainRepository.findById(goal.getDomainId()).orElse(null);
            if (domain != null) {
                domainName = domain.getName();
            }
        }

        return new TaskResponse(
                task.getId(),
                task.getGoalId(),
                goalTitle,
                domainId,
                domainName,
                task.getTitle(),
                task.getFrequency(),
                task.getTaskDate(),
                task.getCompleted(),
                task.getCompletedAt()
        );
    }
}
