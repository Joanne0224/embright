package com.goaltracker.controller;

import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.DomainRequest;
import com.goaltracker.dto.DomainResponse;
import com.goaltracker.service.DomainService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domains")
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    // GET /api/domains -> 首頁卡片列表,依排序順序回傳
    @GetMapping
    public ApiResponse<List<DomainResponse>> getAll() {
        return ApiResponse.ok(domainService.getAll());
    }

    // POST /api/domains -> 新增一個面向(工作/學習/家庭/生活...)
    @PostMapping
    public ApiResponse<DomainResponse> create(@Valid @RequestBody DomainRequest request) {
        return ApiResponse.ok("面向已新增", domainService.create(request));
    }

    // PUT /api/domains/{id} -> 編輯面向名稱/顏色
    @PutMapping("/{id}")
    public ApiResponse<DomainResponse> update(@PathVariable Long id, @Valid @RequestBody DomainRequest request) {
        return ApiResponse.ok("面向已更新", domainService.update(id, request));
    }

    // PATCH /api/domains/{id}/order?direction=UP|DOWN -> 用上下箭頭調整卡片順序
    @PatchMapping("/{id}/order")
    public ApiResponse<List<DomainResponse>> reorder(@PathVariable Long id, @RequestParam String direction) {
        return ApiResponse.ok("順序已調整", domainService.reorder(id, direction));
    }

    // DELETE /api/domains/{id} -> 刪除面向(底下還有目標時會擋下來)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        domainService.delete(id);
        return ApiResponse.ok("面向已刪除", null);
    }
}
