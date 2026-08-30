package com.goaltracker.service;

import com.goaltracker.auth.CurrentUserHolder;
import com.goaltracker.dto.GoalRequest;
import com.goaltracker.dto.GoalResponse;
import com.goaltracker.entity.CompletionLog;
import com.goaltracker.entity.Domain;
import com.goaltracker.entity.Goal;
import com.goaltracker.entity.GoalStatus;
import com.goaltracker.entity.GoalType;
import com.goaltracker.exception.ResourceNotFoundException;
import com.goaltracker.repository.CompletionLogRepository;
import com.goaltracker.repository.DomainRepository;
import com.goaltracker.repository.GoalRepository;
import com.goaltracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 目標(Goal)的商業邏輯層。
 *
 * 資安提醒(這裡曾經有一個真實的漏洞,是自己測試時發現的):
 * 一開始只有「新增目標」時檢查 domainId 是不是自己的,但「查詢」的幾支方法完全沒檢查——
 * 這代表只要拿著別人的 domainId/goalId 去問,後端會照樣把別人的資料吐出來。
 * 現在每一個進來的 id,都要先確認「這真的是你自己的東西」才能繼續,查詢跟寫入都一樣重要。
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final TaskRepository taskRepository;
    private final CoinService coinService;
    private final DomainRepository domainRepository;
    private final CompletionLogRepository completionLogRepository;

    public GoalService(GoalRepository goalRepository, TaskRepository taskRepository,
                        CoinService coinService, DomainRepository domainRepository,
                        CompletionLogRepository completionLogRepository) {
        this.goalRepository = goalRepository;
        this.taskRepository = taskRepository;
        this.coinService = coinService;
        this.domainRepository = domainRepository;
        this.completionLogRepository = completionLogRepository;
    }

    // 目標地圖頁的第一層:查某個面向底下的所有長期目標
    public List<GoalResponse> getByDomainAndType(Long domainId, GoalType type) {
        assertDomainOwned(domainId);
        return goalRepository.findByDomainIdAndType(domainId, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 展開某個目標,看它底下的子目標(長期→中期,中期→短期)
    public List<GoalResponse> getChildren(Long parentId) {
        Goal parent = findOwnedGoalOrThrow(parentId);
        return goalRepository.findByParentId(parent.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoalResponse create(GoalRequest request) {
        validateHierarchy(request.type(), request.parentId());
        assertDomainOwned(request.domainId());

        Goal goal = new Goal();
        goal.setDomainId(request.domainId());
        goal.setParentId(request.parentId());
        goal.setType(request.type());
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetDate(request.targetDate());
        if (request.status() != null) {
            goal.setStatus(request.status());
        }

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    // 確認這個 domainId 真的是目前登入使用者自己的面向
    private void assertDomainOwned(Long domainId) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + domainId + " 的面向"));
        if (!domain.getUserId().equals(CurrentUserHolder.getUserId())) {
            throw new ResourceNotFoundException("找不到 id=" + domainId + " 的面向");
        }
    }

    // 找出這個目標,並確認它所屬的面向真的是目前登入使用者的——這是查詢類方法共用的守門邏輯
    private Goal findOwnedGoalOrThrow(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的目標"));
        assertDomainOwned(goal.getDomainId());
        return goal;
    }

    /**
     * 驗證「這個類型的目標,parentId 給的對不對」:
     * - LONG:不能有 parentId(它自己就是頂層)
     * - MID:parentId 一定要指向一個存在、而且類型是 LONG 的目標
     * - SHORT:parentId 一定要指向一個存在、而且類型是 MID 的目標
     */
    private void validateHierarchy(GoalType type, Long parentId) {
        if (type == GoalType.LONG) {
            if (parentId != null) {
                throw new IllegalArgumentException("長期目標不能有上層目標(parentId 必須是空的)");
            }
            return;
        }

        if (parentId == null) {
            throw new IllegalArgumentException("中期/短期目標一定要指定上層目標(parentId)");
        }

        Goal parent = goalRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + parentId + " 的上層目標"));

        GoalType expectedParentType = (type == GoalType.MID) ? GoalType.LONG : GoalType.MID;
        if (parent.getType() != expectedParentType) {
            throw new IllegalArgumentException(
                    type + " 目標的上層目標必須是 " + expectedParentType + " 類型,但 id=" + parentId + " 是 " + parent.getType()
            );
        }
    }

    public GoalResponse update(Long id, GoalRequest request) {
        Goal goal = findOwnedGoalOrThrow(id);

        // 只允許改內容,不允許改類型/上層目標/面向(避免把樹狀結構改亂,簡化版邏輯)
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetDate(request.targetDate());
        if (request.status() != null) {
            goal.setStatus(request.status());
        }

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    // 完成一個目標(通常是短期目標全部任務做完後,手動標記整個目標達成)
    // 完成目標會發放「雙倍」金幣獎勵,比完成單一任務更有份量,呼應長期目標更難達成這件事
    // 也會寫進 completion_logs(taskId 留空,因為這不是針對某一筆任務,是整個目標達成的紀錄)
    @Transactional
    public GoalResponse markCompleted(Long id) {
        Goal goal = findOwnedGoalOrThrow(id);
        goal.setStatus(GoalStatus.COMPLETED);
        GoalResponse response = toResponse(goalRepository.save(goal));

        CompletionLog log = new CompletionLog();
        log.setTaskId(null);
        log.setGoalId(goal.getId());
        log.setDomainId(goal.getDomainId());
        log.setCompletedDate(LocalDate.now());
        log.setNote("目標達成:" + goal.getTitle());
        completionLogRepository.save(log);

        coinService.awardGoalComplete(goal.getId());
        return response;
    }

    // 遞迴刪除:刪掉這個目標,也刪掉它底下所有子目標跟任務
    // (completion_logs 刻意不刪除,保留永久歷史紀錄——查詢時會用 id 去找標題,
    //  找不到就顯示「已刪除」,詳見 CompletionLogService)
    public void delete(Long id) {
        Goal goal = findOwnedGoalOrThrow(id);
        deleteRecursive(goal);
    }

    private void deleteRecursive(Goal goal) {
        List<Goal> children = goalRepository.findByParentId(goal.getId());
        for (Goal child : children) {
            deleteRecursive(child);
        }
        taskRepository.deleteAll(taskRepository.findByGoalId(goal.getId()));
        goalRepository.delete(goal);
    }

    private GoalResponse toResponse(Goal goal) {
        long childCount = goalRepository.countByParentId(goal.getId());
        return new GoalResponse(
                goal.getId(),
                goal.getDomainId(),
                goal.getParentId(),
                goal.getType(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getTargetDate(),
                goal.getStatus(),
                childCount
        );
    }
}
