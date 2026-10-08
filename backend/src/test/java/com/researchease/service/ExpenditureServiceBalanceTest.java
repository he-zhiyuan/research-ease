package com.researchease.service;

import com.researchease.common.BusinessException;
import com.researchease.dto.ExpenditureRequest;
import com.researchease.dto.RejectRequest;
import com.researchease.entity.BudgetItem;
import com.researchease.entity.ApprovalRecord;
import com.researchease.entity.Expenditure;
import com.researchease.entity.Project;
import com.researchease.enums.ExpenditureStatus;
import com.researchease.enums.ProjectStatus;
import com.researchease.mapper.ApprovalRecordMapper;
import com.researchease.mapper.BudgetItemMapper;
import com.researchease.mapper.ExpenditureMapper;
import com.researchease.mapper.ProjectMapper;
import com.researchease.mapper.UserMapper;
import com.researchease.service.impl.ExpenditureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 覆盖《数据模型与接口设计.md》§1.4 经费余额计算规则：
 * 余额 = approved_amount - SUM(APPROVED) - SUM(PENDING)，REJECTED 不计入占用。
 */
@ExtendWith(MockitoExtension.class)
class ExpenditureServiceBalanceTest {

    @Mock
    private ExpenditureMapper expenditureMapper;
    @Mock
    private BudgetItemMapper budgetItemMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ApprovalRecordMapper approvalRecordMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ExpenditureServiceImpl expenditureService;

    private static final Long LEADER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long BUDGET_ITEM_ID = 200L;

    private Project executingProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setLeaderId(LEADER_ID);
        project.setStatus(ProjectStatus.EXECUTING.name());
        return project;
    }

    private BudgetItem budgetItem(BigDecimal approvedAmount) {
        BudgetItem item = new BudgetItem();
        item.setId(BUDGET_ITEM_ID);
        item.setProjectId(PROJECT_ID);
        item.setCategory("EQUIPMENT");
        item.setApprovedAmount(approvedAmount);
        return item;
    }

    private Expenditure expenditureOf(BigDecimal amount) {
        Expenditure e = new Expenditure();
        e.setAmount(amount);
        return e;
    }

    private ExpenditureRequest requestOf(BigDecimal amount) {
        ExpenditureRequest request = new ExpenditureRequest();
        request.setBudgetItemId(BUDGET_ITEM_ID);
        request.setAmount(amount);
        request.setDescription("测试支出");
        return request;
    }

    @Test
    void 余额充足时提交支出申请应成功且状态为pending() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(executingProject());
        when(budgetItemMapper.selectById(BUDGET_ITEM_ID)).thenReturn(budgetItem(new BigDecimal("1000")));
        // 已批准合计200 + 审批中合计300 => 余额500
        when(expenditureMapper.selectList(any()))
                .thenReturn(List.of(expenditureOf(new BigDecimal("200"))))
                .thenReturn(List.of(expenditureOf(new BigDecimal("300"))));

        expenditureService.submit(PROJECT_ID, requestOf(new BigDecimal("400")), LEADER_ID);

        ArgumentCaptor<Expenditure> captor = ArgumentCaptor.forClass(Expenditure.class);
        verify(expenditureMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ExpenditureStatus.PENDING.name());
        assertThat(captor.getValue().getAmount()).isEqualTo(new BigDecimal("400"));
    }

    @Test
    void 余额不足时提交支出申请应被拒绝() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(executingProject());
        when(budgetItemMapper.selectById(BUDGET_ITEM_ID)).thenReturn(budgetItem(new BigDecimal("1000")));
        when(expenditureMapper.selectList(any()))
                .thenReturn(List.of(expenditureOf(new BigDecimal("200"))))
                .thenReturn(List.of(expenditureOf(new BigDecimal("300"))));

        assertThatThrownBy(() -> expenditureService.submit(PROJECT_ID, requestOf(new BigDecimal("600")), LEADER_ID))
                .isInstanceOf(BusinessException.class);

        verify(expenditureMapper, never()).insert(any(Expenditure.class));
    }

    @Test
    void 已驳回的支出不计入已占用额度() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(executingProject());
        when(budgetItemMapper.selectById(BUDGET_ITEM_ID)).thenReturn(budgetItem(new BigDecimal("1000")));
        // 历史上有一笔被驳回的支出不会出现在 APPROVED/PENDING 的查询结果里，因此余额仍是满额
        when(expenditureMapper.selectList(any()))
                .thenReturn(List.of())
                .thenReturn(List.of());

        expenditureService.submit(PROJECT_ID, requestOf(new BigDecimal("1000")), LEADER_ID);

        verify(expenditureMapper).insert(any(Expenditure.class));
    }

    @Test
    void 驳回支出申请后状态变为rejected并写入审批记录() {
        Expenditure expenditure = new Expenditure();
        expenditure.setId(300L);
        expenditure.setProjectId(PROJECT_ID);
        expenditure.setStatus(ExpenditureStatus.PENDING.name());

        when(expenditureMapper.selectById(300L)).thenReturn(expenditure);
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(executingProject());

        RejectRequest request = new RejectRequest();
        request.setComment("凭证不全");
        expenditureService.reject(300L, request, 99L);

        assertThat(expenditure.getStatus()).isEqualTo(ExpenditureStatus.REJECTED.name());
        verify(approvalRecordMapper).insert(any(ApprovalRecord.class));
    }

    @Test
    void 项目非执行中状态时不允许审核经费支出() {
        Expenditure expenditure = new Expenditure();
        expenditure.setId(300L);
        expenditure.setProjectId(PROJECT_ID);
        expenditure.setStatus(ExpenditureStatus.PENDING.name());

        Project completedProject = executingProject();
        completedProject.setStatus(ProjectStatus.COMPLETED.name());

        when(expenditureMapper.selectById(300L)).thenReturn(expenditure);
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(completedProject);

        assertThatThrownBy(() -> expenditureService.approve(300L, 99L))
                .isInstanceOf(BusinessException.class);
    }
}
