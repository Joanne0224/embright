package com.goaltracker.service;

import com.goaltracker.dto.DomainRequest;
import com.goaltracker.dto.DomainResponse;
import com.goaltracker.entity.Domain;
import com.goaltracker.entity.GoalType;
import com.goaltracker.exception.ResourceNotFoundException;
import com.goaltracker.repository.DomainRepository;
import com.goaltracker.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 面向(Domain)的商業邏輯層。
 *
 * 為什麼 Controller 不直接呼叫 Repository、要多一層 Service?(呼應第10、17、18堂筆記)
 * - Controller 只負責「接請求、回응」,不該知道資料庫怎麼查
 * - 像「算這個面向有幾個長期目標」這種跨表邏輯,屬於商業邏輯,放 Service 層才對
 * - 之後如果要加規則(例如:面向名稱不能重複),只要改 Service,Controller 完全不用動
 */
@Service
public class DomainService {

    private final DomainRepository domainRepository;
    private final GoalRepository goalRepository;

    public DomainService(DomainRepository domainRepository, GoalRepository goalRepository) {
        this.domainRepository = domainRepository;
        this.goalRepository = goalRepository;
    }

    public List<DomainResponse> getAll() {
        return domainRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DomainResponse create(DomainRequest request) {
        Domain domain = new Domain();
        domain.setName(request.name());
        domain.setColor(request.color() != null ? request.color() : "blue");

        // 沒指定排序值的話,自動排到最後面(目前最大值 + 1)
        int nextOrder = domainRepository.findAllByOrderBySortOrderAsc().stream()
                .mapToInt(Domain::getSortOrder)
                .max()
                .orElse(-1) + 1;
        domain.setSortOrder(request.sortOrder() != null ? request.sortOrder() : nextOrder);

        Domain saved = domainRepository.save(domain);
        return toResponse(saved);
    }

    public DomainResponse update(Long id, DomainRequest request) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的面向"));

        domain.setName(request.name());
        if (request.color() != null) domain.setColor(request.color());
        if (request.sortOrder() != null) domain.setSortOrder(request.sortOrder());

        Domain saved = domainRepository.save(domain);
        return toResponse(saved);
    }

    // 上下箭頭調整順序:跟相鄰的那個面向交換 sortOrder 值
    public List<DomainResponse> reorder(Long id, String direction) {
        List<Domain> all = domainRepository.findAllByOrderBySortOrderAsc();
        int index = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new ResourceNotFoundException("找不到 id=" + id + " 的面向");
        }

        int targetIndex = "UP".equalsIgnoreCase(direction) ? index - 1 : index + 1;
        if (targetIndex < 0 || targetIndex >= all.size()) {
            // 已經在最上面/最下面了,不用交換,直接回傳現況
            return getAll();
        }

        Domain current = all.get(index);
        Domain target = all.get(targetIndex);
        int tempOrder = current.getSortOrder();
        current.setSortOrder(target.getSortOrder());
        target.setSortOrder(tempOrder);

        domainRepository.save(current);
        domainRepository.save(target);

        return getAll();
    }

    public void delete(Long id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的面向"));

        boolean hasGoals = !goalRepository.findByDomainId(id).isEmpty();
        if (hasGoals) {
            throw new IllegalArgumentException("這個面向底下還有目標,請先清空目標才能刪除面向");
        }

        domainRepository.delete(domain);
    }

    private DomainResponse toResponse(Domain domain) {
        long longGoalCount = goalRepository.findByDomainIdAndType(domain.getId(), GoalType.LONG).size();
        return new DomainResponse(
                domain.getId(),
                domain.getName(),
                domain.getColor(),
                domain.getSortOrder(),
                longGoalCount
        );
    }
}
