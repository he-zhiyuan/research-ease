package com.researchease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.researchease.common.BusinessException;
import com.researchease.dto.ApprovalRecordResponse;
import com.researchease.dto.ApproveProjectRequest;
import com.researchease.dto.BudgetApprovalItem;
import com.researchease.dto.BudgetItemRequest;
import com.researchease.dto.BudgetItemResponse;
import com.researchease.dto.ProjectCreateRequest;
import com.researchease.dto.ProjectResponse;
import com.researchease.dto.ProjectUpdateRequest;
import com.researchease.dto.RejectRequest;
import com.researchease.entity.ApprovalRecord;
import com.researchease.entity.BudgetItem;
import com.researchease.entity.Project;
import com.researchease.entity.User;
import com.researchease.enums.ApprovalResult;
import com.researchease.enums.ApprovalType;
import com.researchease.enums.ProjectStatus;
import com.researchease.enums.Role;
import com.researchease.mapper.ApprovalRecordMapper;
import com.researchease.mapper.BudgetItemMapper;
import com.researchease.mapper.ProjectMapper;
import com.researchease.mapper.UserMapper;
import com.researchease.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final BudgetItemMapper budgetItemMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ProjectResponse create(ProjectCreateRequest request, Long leaderId) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setResearchContent(request.getResearchContent());
        project.setLeaderId(leaderId);
        project.setTeamMembers(request.getTeamMembers());
        project.setFundingSource(request.getFundingSource());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(ProjectStatus.DRAFT.name());
        projectMapper.insert(project);

        insertBudgetItems(project.getId(), request.getBudgetItems());

        return getById(project.getId(), leaderId, Role.RESEARCHER.name());
    }

    @Override
    @Transactional
    public void update(Long projectId, ProjectUpdateRequest request, Long currentUserId) {
        Project project = requireProject(projectId);
        requireLeader(project, currentUserId);
        ProjectStatus status = ProjectStatus.valueOf(project.getStatus());
        if (status != ProjectStatus.DRAFT && status != ProjectStatus.REJECTED) {
            throw new BusinessException("仅草稿或已驳回状态的项目可以修改");
        }

        project.setProjectName(request.getProjectName());
        project.setResearchContent(request.getResearchContent());
        project.setTeamMembers(request.getTeamMembers());
        project.setFundingSource(request.getFundingSource());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        projectMapper.updateById(project);

        budgetItemMapper.delete(new LambdaQueryWrapper<BudgetItem>().eq(BudgetItem::getProjectId, projectId));
        insertBudgetItems(projectId, request.getBudgetItems());
    }

    @Override
    @Transactional
    public void submit(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        requireLeader(project, currentUserId);
        ProjectStatus status = ProjectStatus.valueOf(project.getStatus());
        if (status != ProjectStatus.DRAFT && status != ProjectStatus.REJECTED) {
            throw new BusinessException("当前状态[" + status + "]不允许提交");
        }
        project.setStatus(ProjectStatus.SUBMITTED.name());
        project.setSubmittedAt(LocalDateTime.now());
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void approve(Long projectId, ApproveProjectRequest request, Long operatorId) {
        Project project = requireProject(projectId);
        ProjectStatus status = ProjectStatus.valueOf(project.getStatus());
        if (status != ProjectStatus.SUBMITTED) {
            throw new BusinessException("当前状态[" + status + "]不允许审批通过");
        }

        Map<Long, BigDecimal> overrides = new HashMap<>();
        if (request != null && request.getBudgetItems() != null) {
            for (BudgetApprovalItem item : request.getBudgetItems()) {
                overrides.put(item.getBudgetItemId(), item.getApprovedAmount());
            }
        }
        List<BudgetItem> items = budgetItemMapper.selectList(
                new LambdaQueryWrapper<BudgetItem>().eq(BudgetItem::getProjectId, projectId));
        for (BudgetItem item : items) {
            BigDecimal approvedAmount = overrides.getOrDefault(item.getId(), item.getRequestedAmount());
            item.setApprovedAmount(approvedAmount);
            budgetItemMapper.updateById(item);
        }

        project.setStatus(ProjectStatus.EXECUTING.name());
        projectMapper.updateById(project);

        recordApproval(projectId, ApprovalType.PROJECT_APPROVAL, null, ApprovalResult.APPROVED, null, operatorId);
    }

    @Override
    @Transactional
    public void reject(Long projectId, RejectRequest request, Long operatorId) {
        Project project = requireProject(projectId);
        ProjectStatus status = ProjectStatus.valueOf(project.getStatus());
        if (status != ProjectStatus.SUBMITTED) {
            throw new BusinessException("当前状态[" + status + "]不允许驳回");
        }
        project.setStatus(ProjectStatus.REJECTED.name());
        projectMapper.updateById(project);

        recordApproval(projectId, ApprovalType.PROJECT_APPROVAL, null, ApprovalResult.REJECTED, request.getComment(), operatorId);
    }

    @Override
    public ProjectResponse getById(Long projectId, Long currentUserId, String currentRole) {
        Project project = requireProject(projectId);
        if (Role.RESEARCHER.name().equals(currentRole)) {
            requireLeader(project, currentUserId);
        }
        return toResponse(project);
    }

    @Override
    public List<ProjectResponse> listMy(Long currentUserId) {
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getLeaderId, currentUserId)
                        .orderByDesc(Project::getCreatedAt));
        return projects.stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProjectResponse> listForManager(String status, String keyword) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .orderByDesc(Project::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Project::getStatus, status);
        }
        List<Project> projects = projectMapper.selectList(wrapper);
        List<ProjectResponse> responses = projects.stream().map(this::toResponse).toList();
        if (keyword == null || keyword.isBlank()) {
            return responses;
        }
        String kw = keyword.trim();
        return responses.stream()
                .filter(r -> r.getProjectName().contains(kw) || (r.getLeaderName() != null && r.getLeaderName().contains(kw)))
                .toList();
    }

    @Override
    public List<ApprovalRecordResponse> listApprovalRecords(Long projectId, Long currentUserId, String currentRole) {
        Project project = requireProject(projectId);
        if (Role.RESEARCHER.name().equals(currentRole)) {
            requireLeader(project, currentUserId);
        }
        List<ApprovalRecord> records = approvalRecordMapper.selectList(
                new LambdaQueryWrapper<ApprovalRecord>().eq(ApprovalRecord::getProjectId, projectId)
                        .orderByDesc(ApprovalRecord::getCreatedAt));
        return records.stream().map(this::toApprovalResponse).toList();
    }

    private void insertBudgetItems(Long projectId, List<BudgetItemRequest> items) {
        for (BudgetItemRequest item : items) {
            BudgetItem entity = new BudgetItem();
            entity.setProjectId(projectId);
            entity.setCategory(item.getCategory().name());
            entity.setRequestedAmount(item.getRequestedAmount());
            entity.setApprovedAmount(null);
            budgetItemMapper.insert(entity);
        }
    }

    private Project requireProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    private void requireLeader(Project project, Long currentUserId) {
        if (!project.getLeaderId().equals(currentUserId)) {
            throw new BusinessException(403, "仅项目负责人本人可执行该操作");
        }
    }

    private void recordApproval(Long projectId, ApprovalType type, Long targetId, ApprovalResult result,
                                 String comment, Long operatorId) {
        ApprovalRecord record = new ApprovalRecord();
        record.setProjectId(projectId);
        record.setType(type.name());
        record.setTargetId(targetId);
        record.setResult(result.name());
        record.setComment(comment);
        record.setOperatorId(operatorId);
        approvalRecordMapper.insert(record);
    }

    private ProjectResponse toResponse(Project project) {
        User leader = userMapper.selectById(project.getLeaderId());
        List<BudgetItem> items = budgetItemMapper.selectList(
                new LambdaQueryWrapper<BudgetItem>().eq(BudgetItem::getProjectId, project.getId()));
        List<BudgetItemResponse> itemResponses = items.stream()
                .map(i -> new BudgetItemResponse(i.getId(), i.getCategory(), i.getRequestedAmount(), i.getApprovedAmount()))
                .toList();
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .researchContent(project.getResearchContent())
                .leaderId(project.getLeaderId())
                .leaderName(leader != null ? leader.getRealName() : null)
                .teamMembers(project.getTeamMembers())
                .fundingSource(project.getFundingSource())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .submittedAt(project.getSubmittedAt())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .budgetItems(itemResponses)
                .build();
    }

    private ApprovalRecordResponse toApprovalResponse(ApprovalRecord record) {
        User operator = userMapper.selectById(record.getOperatorId());
        return new ApprovalRecordResponse(record.getId(), record.getType(), record.getTargetId(),
                record.getResult(), record.getComment(), record.getOperatorId(),
                operator != null ? operator.getRealName() : null, record.getCreatedAt());
    }
}
