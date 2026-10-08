package com.researchease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.researchease.common.BusinessException;
import com.researchease.dto.BudgetItemBalanceResponse;
import com.researchease.dto.ExpenditureRequest;
import com.researchease.dto.ExpenditureResponse;
import com.researchease.dto.RejectRequest;
import com.researchease.entity.ApprovalRecord;
import com.researchease.entity.BudgetItem;
import com.researchease.entity.Expenditure;
import com.researchease.entity.Project;
import com.researchease.entity.User;
import com.researchease.enums.ApprovalResult;
import com.researchease.enums.ApprovalType;
import com.researchease.enums.ExpenditureStatus;
import com.researchease.enums.ProjectStatus;
import com.researchease.enums.Role;
import com.researchease.mapper.ApprovalRecordMapper;
import com.researchease.mapper.BudgetItemMapper;
import com.researchease.mapper.ExpenditureMapper;
import com.researchease.mapper.ProjectMapper;
import com.researchease.mapper.UserMapper;
import com.researchease.service.ExpenditureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenditureServiceImpl implements ExpenditureService {

    private final ExpenditureMapper expenditureMapper;
    private final BudgetItemMapper budgetItemMapper;
    private final ProjectMapper projectMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ExpenditureResponse submit(Long projectId, ExpenditureRequest request, Long currentUserId) {
        Project project = requireProject(projectId);
        requireLeader(project, currentUserId);
        if (ProjectStatus.valueOf(project.getStatus()) != ProjectStatus.EXECUTING) {
            throw new BusinessException("仅执行中的项目可以提交经费支出申请");
        }

        BudgetItem budgetItem = budgetItemMapper.selectById(request.getBudgetItemId());
        if (budgetItem == null || !budgetItem.getProjectId().equals(projectId)) {
            throw new BusinessException("预算科目不存在");
        }

        BigDecimal balance = calculateBalance(budgetItem);
        if (request.getAmount().compareTo(balance) > 0) {
            throw new BusinessException("预算余额不足，当前可用余额为 " + balance);
        }

        Expenditure expenditure = new Expenditure();
        expenditure.setProjectId(projectId);
        expenditure.setBudgetItemId(request.getBudgetItemId());
        expenditure.setAmount(request.getAmount());
        expenditure.setDescription(request.getDescription());
        expenditure.setStatus(ExpenditureStatus.PENDING.name());
        expenditure.setCreatedBy(currentUserId);
        expenditureMapper.insert(expenditure);

        return toResponse(expenditure);
    }

    @Override
    public List<ExpenditureResponse> listForProject(Long projectId, Long currentUserId, String currentRole) {
        Project project = requireProject(projectId);
        if (Role.RESEARCHER.name().equals(currentRole)) {
            requireLeader(project, currentUserId);
        }
        List<Expenditure> expenditures = expenditureMapper.selectList(
                new LambdaQueryWrapper<Expenditure>().eq(Expenditure::getProjectId, projectId)
                        .orderByDesc(Expenditure::getCreatedAt));
        return expenditures.stream().map(this::toResponse).toList();
    }

    @Override
    public List<ExpenditureResponse> listByStatus(String status) {
        String target = (status == null || status.isBlank()) ? ExpenditureStatus.PENDING.name() : status;
        List<Expenditure> expenditures = expenditureMapper.selectList(
                new LambdaQueryWrapper<Expenditure>().eq(Expenditure::getStatus, target)
                        .orderByDesc(Expenditure::getCreatedAt));
        return expenditures.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void approve(Long expenditureId, Long operatorId) {
        Expenditure expenditure = requireExpenditure(expenditureId);
        requireExecutingProject(expenditure.getProjectId());
        if (ExpenditureStatus.valueOf(expenditure.getStatus()) != ExpenditureStatus.PENDING) {
            throw new BusinessException("该支出申请已被处理");
        }
        expenditure.setStatus(ExpenditureStatus.APPROVED.name());
        expenditureMapper.updateById(expenditure);
        recordApproval(expenditure.getProjectId(), expenditureId, ApprovalResult.APPROVED, null, operatorId);
    }

    @Override
    @Transactional
    public void reject(Long expenditureId, RejectRequest request, Long operatorId) {
        Expenditure expenditure = requireExpenditure(expenditureId);
        requireExecutingProject(expenditure.getProjectId());
        if (ExpenditureStatus.valueOf(expenditure.getStatus()) != ExpenditureStatus.PENDING) {
            throw new BusinessException("该支出申请已被处理");
        }
        expenditure.setStatus(ExpenditureStatus.REJECTED.name());
        expenditureMapper.updateById(expenditure);
        recordApproval(expenditure.getProjectId(), expenditureId, ApprovalResult.REJECTED, request.getComment(), operatorId);
    }

    @Override
    public List<BudgetItemBalanceResponse> listBudgetItemsWithBalance(Long projectId, Long currentUserId, String currentRole) {
        Project project = requireProject(projectId);
        if (Role.RESEARCHER.name().equals(currentRole)) {
            requireLeader(project, currentUserId);
        }
        List<BudgetItem> items = budgetItemMapper.selectList(
                new LambdaQueryWrapper<BudgetItem>().eq(BudgetItem::getProjectId, projectId));
        return items.stream()
                .map(item -> new BudgetItemBalanceResponse(item.getId(), item.getCategory(),
                        item.getRequestedAmount(), item.getApprovedAmount(), calculateBalance(item)))
                .toList();
    }

    private BigDecimal calculateBalance(BudgetItem budgetItem) {
        if (budgetItem.getApprovedAmount() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal approved = sumByBudgetItemAndStatus(budgetItem.getId(), ExpenditureStatus.APPROVED.name());
        BigDecimal pending = sumByBudgetItemAndStatus(budgetItem.getId(), ExpenditureStatus.PENDING.name());
        return budgetItem.getApprovedAmount().subtract(approved).subtract(pending);
    }

    private BigDecimal sumByBudgetItemAndStatus(Long budgetItemId, String status) {
        List<Expenditure> expenditures = expenditureMapper.selectList(
                new LambdaQueryWrapper<Expenditure>()
                        .eq(Expenditure::getBudgetItemId, budgetItemId)
                        .eq(Expenditure::getStatus, status));
        return expenditures.stream().map(Expenditure::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private void requireExecutingProject(Long projectId) {
        Project project = requireProject(projectId);
        if (ProjectStatus.valueOf(project.getStatus()) != ProjectStatus.EXECUTING) {
            throw new BusinessException("仅执行中的项目可以审核经费支出");
        }
    }

    private Expenditure requireExpenditure(Long expenditureId) {
        Expenditure expenditure = expenditureMapper.selectById(expenditureId);
        if (expenditure == null) {
            throw new BusinessException(404, "支出申请不存在");
        }
        return expenditure;
    }

    private void recordApproval(Long projectId, Long targetId, ApprovalResult result, String comment, Long operatorId) {
        ApprovalRecord record = new ApprovalRecord();
        record.setProjectId(projectId);
        record.setType(ApprovalType.FUND_APPROVAL.name());
        record.setTargetId(targetId);
        record.setResult(result.name());
        record.setComment(comment);
        record.setOperatorId(operatorId);
        approvalRecordMapper.insert(record);
    }

    private ExpenditureResponse toResponse(Expenditure expenditure) {
        BudgetItem budgetItem = budgetItemMapper.selectById(expenditure.getBudgetItemId());
        User creator = userMapper.selectById(expenditure.getCreatedBy());
        return new ExpenditureResponse(expenditure.getId(), expenditure.getProjectId(), expenditure.getBudgetItemId(),
                budgetItem != null ? budgetItem.getCategory() : null, expenditure.getAmount(), expenditure.getDescription(),
                expenditure.getStatus(), expenditure.getCreatedBy(), creator != null ? creator.getRealName() : null,
                expenditure.getCreatedAt());
    }
}
