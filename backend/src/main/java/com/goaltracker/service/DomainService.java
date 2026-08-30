package com.goaltracker.service;

import com.goaltracker.auth.CurrentUserHolder;
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
 * 加了登入系統之後,每一個方法都要多做一件事:「只處理屬於目前登入使用者的資料」。
 * CurrentUserHolder.getUserId() 會拿到 AuthFilter 事先解析好的使用者 id——
 * 這樣 Controller 完全不用改,呼叫方式跟以前一模一樣,商業邏輯層自己把「是誰在問」這件事處理掉。
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
        Long userId = CurrentUserHolder.getUserId();
        return domainRepository.findAllByUserIdOrderBySortOrderAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DomainResponse create(DomainRequest request) {
        Long userId = CurrentUserHolder.getUserId();

        Domain domain = new Domain();
        domain.setUserId(userId);
        domain.setName(request.name());
        domain.setColor(request.color() != null ? request.color() : "blue");

        // 沒指定排序值的話,自動排到最後面(這個使用者目前最大值 + 1)
        int nextOrder = domainRepository.findAllByUserIdOrderBySortOrderAsc(userId).stream()
                .mapToInt(Domain::getSortOrder)
                .max()
                .orElse(-1) + 1;
        domain.setSortOrder(request.sortOrder() != null ? request.sortOrder() : nextOrder);

        Domain saved = domainRepository.save(domain);
        return toResponse(saved);
    }

    public DomainResponse update(Long id, DomainRequest request) {
        Domain domain = findOwnedOrThrow(id);

        domain.setName(request.name());
        if (request.color() != null) domain.setColor(request.color());
        if (request.sortOrder() != null) domain.setSortOrder(request.sortOrder());

        Domain saved = domainRepository.save(domain);
        return toResponse(saved);
    }

    // 上下箭頭調整順序:跟相鄰的那個面向交換 sortOrder 值
    public List<DomainResponse> reorder(Long id, String direction) {
        Long userId = CurrentUserHolder.getUserId();
        findOwnedOrThrow(id); // 確認這個面向真的是這個使用者的,不是別人的

        List<Domain> all = domainRepository.findAllByUserIdOrderBySortOrderAsc(userId);
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
        Domain domain = findOwnedOrThrow(id);

        boolean hasGoals = !goalRepository.findByDomainId(id).isEmpty();
        if (hasGoals) {
            throw new IllegalArgumentException("這個面向底下還有目標,請先清空目標才能刪除面向");
        }

        domainRepository.delete(domain);
    }

    // 找出這個面向,並且順便確認它真的屬於目前登入的使用者——
    // 沒有這一層檢查的話,登入的人 A 理論上可以用 id 猜、去改到使用者 B 的資料,這是資安漏洞
    private Domain findOwnedOrThrow(Long id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的面向"));
        if (!domain.getUserId().equals(CurrentUserHolder.getUserId())) {
            throw new ResourceNotFoundException("找不到 id=" + id + " 的面向");
        }
        return domain;
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
