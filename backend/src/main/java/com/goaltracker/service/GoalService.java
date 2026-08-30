package com.goaltracker.service;

import com.goaltracker.dto.GoalRequest;
import com.goaltracker.dto.GoalResponse;
import com.goaltracker.entity.Goal;
import com.goaltracker.entity.GoalStatus;
import com.goaltracker.entity.GoalType;
import com.goaltracker.exception.ResourceNotFoundException;
import com.goaltracker.repository.GoalRepository;
import com.goaltracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 目標(Goal)的商業邏輯層。
 *
 * 這裡是整個系統「防呆」最重要的地方:資料庫本身沒辦法用簡單的 CHECK 約束
 * 表達「短期目標的 parentId 一定要指向一個中期目標」這種跨欄位規則,
 * 所以由這一層的 Java 邏輯把關——這就是第14堂筆記講的「FK 守住的是資料能不能被信任」。
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final TaskRepository taskRepository;
    private final CoinService coinService;

    public GoalService(GoalRepository goalRepository, TaskRepository taskRepository, CoinService coinService) {
        this.goalRepository = goalRepository;
        this.taskRepository = taskRepository;
        this.coinService = coinService;
    }

    // 目標地圖頁的第一層:查某個面向底下的所有長期目標
    public List<GoalResponse> getByDomainAndType(Long domainId, GoalType type) {
        return goalRepository.findByDomainIdAndType(domainId, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 展開某個目標,看它底下的子目標(長期→中期,中期→短期)
    public List<GoalResponse> getChildren(Long parentId) {
        return goalRepository.findByParentId(parentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoalResponse create(GoalRequest request) {
        validateHierarchy(request.type(), request.parentId());

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
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的目標"));

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
    @Transactional
    public GoalResponse markCompleted(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的目標"));
        goal.setStatus(GoalStatus.COMPLETED);
        GoalResponse response = toResponse(goalRepository.save(goal));
        coinService.awardGoalComplete(goal.getId());
        return response;
    }

    // 遞迴刪除:刪掉這個目標,也刪掉它底下所有子目標跟任務
    // (completion_logs 刻意不刪除,保留永久歷史紀錄——查詢時會用 id 去找標題,
    //  找不到就顯示「已刪除」,詳見 CompletionLogService)
    public void delete(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的目標"));
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
