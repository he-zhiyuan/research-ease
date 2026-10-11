package com.researchease.controller;

import com.researchease.common.LoginUser;
import com.researchease.common.Result;
import com.researchease.common.SecurityUtils;
import com.researchease.dto.AcceptanceRequest;
import com.researchease.dto.AchievementResponse;
import com.researchease.dto.ApprovalRecordResponse;
import com.researchease.dto.ApproveProjectRequest;
import com.researchease.dto.MyApprovalRecordResponse;
import com.researchease.dto.ProjectCreateRequest;
import com.researchease.dto.ProjectResponse;
import com.researchease.dto.ProjectUpdateRequest;
import com.researchease.dto.RejectRequest;
import com.researchease.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        return Result.ok(projectService.create(request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        projectService.update(id, request, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<Void> submit(@PathVariable Long id) {
        projectService.submit(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<List<ProjectResponse>> listMy() {
        return Result.ok(projectService.listMy(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/my/approval-records")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<List<MyApprovalRecordResponse>> myApprovalRecords() {
        return Result.ok(projectService.listMyApprovalRecords(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Result<List<ProjectResponse>> list(@RequestParam(required = false) String status,
                                               @RequestParam(required = false) String keyword) {
        return Result.ok(projectService.listForManager(status, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESEARCHER','MANAGER')")
    public Result<ProjectResponse> getById(@PathVariable Long id) {
        LoginUser current = SecurityUtils.getCurrentUser();
        return Result.ok(projectService.getById(id, current.getUserId(), current.getRole()));
    }

    @GetMapping("/{id}/approval-records")
    @PreAuthorize("hasAnyRole('RESEARCHER','MANAGER')")
    public Result<List<ApprovalRecordResponse>> approvalRecords(@PathVariable Long id) {
        LoginUser current = SecurityUtils.getCurrentUser();
        return Result.ok(projectService.listApprovalRecords(id, current.getUserId(), current.getRole()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) ApproveProjectRequest request) {
        projectService.approve(id, request, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        projectService.reject(id, request, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/acceptance")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<Void> submitAcceptance(@PathVariable Long id, @Valid @RequestBody AcceptanceRequest request) {
        projectService.submitAcceptance(id, request, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/{id}/achievements")
    @PreAuthorize("hasAnyRole('RESEARCHER','MANAGER')")
    public Result<List<AchievementResponse>> achievements(@PathVariable Long id) {
        LoginUser current = SecurityUtils.getCurrentUser();
        return Result.ok(projectService.listAchievements(id, current.getUserId(), current.getRole()));
    }

    @PostMapping("/{id}/acceptance/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<Void> approveAcceptance(@PathVariable Long id) {
        projectService.approveAcceptance(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/acceptance/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<Void> rejectAcceptance(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        projectService.rejectAcceptance(id, request, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}
