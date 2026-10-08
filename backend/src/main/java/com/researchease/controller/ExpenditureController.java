package com.researchease.controller;

import com.researchease.common.Result;
import com.researchease.common.SecurityUtils;
import com.researchease.dto.BudgetItemBalanceResponse;
import com.researchease.dto.ExpenditureRequest;
import com.researchease.dto.ExpenditureResponse;
import com.researchease.dto.RejectRequest;
import com.researchease.service.ExpenditureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @PostMapping("/api/projects/{id}/expenditures")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<ExpenditureResponse> submit(@PathVariable Long id, @Valid @RequestBody ExpenditureRequest request) {
        return Result.ok(expenditureService.submit(id, request, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/api/projects/{id}/expenditures")
    @PreAuthorize("hasAnyRole('RESEARCHER','MANAGER')")
    public Result<List<ExpenditureResponse>> listForProject(@PathVariable Long id) {
        return Result.ok(expenditureService.listForProject(id, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentRole()));
    }

    @GetMapping("/api/projects/{id}/budget-items")
    @PreAuthorize("hasAnyRole('RESEARCHER','MANAGER')")
    public Result<List<BudgetItemBalanceResponse>> listBudgetItems(@PathVariable Long id) {
        return Result.ok(expenditureService.listBudgetItemsWithBalance(id, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentRole()));
    }

    @GetMapping("/api/expenditures")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<List<ExpenditureResponse>> listByStatus(@RequestParam(required = false) String status) {
        return Result.ok(expenditureService.listByStatus(status));
    }

    @PostMapping("/api/expenditures/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<Void> approve(@PathVariable Long id) {
        expenditureService.approve(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/api/expenditures/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        expenditureService.reject(id, request, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}
