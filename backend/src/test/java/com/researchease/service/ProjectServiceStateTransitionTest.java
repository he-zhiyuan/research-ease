package com.researchease.service;

import com.researchease.common.BusinessException;
import com.researchease.dto.AcceptanceRequest;
import com.researchease.dto.AchievementRequest;
import com.researchease.dto.ApproveProjectRequest;
import com.researchease.dto.RejectRequest;
import com.researchease.entity.Project;
import com.researchease.enums.AchievementType;
import com.researchease.enums.ProjectStatus;
import com.researchease.mapper.AchievementMapper;
import com.researchease.mapper.ApprovalRecordMapper;
import com.researchease.mapper.BudgetItemMapper;
import com.researchease.mapper.ProjectMapper;
import com.researchease.mapper.UserMapper;
import com.researchease.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 覆盖《数据模型与接口设计.md》§3 状态跳转合法性表里的每一条合法跳转，
 * 以及若干代表性的非法跳转。
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceStateTransitionTest {

    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private BudgetItemMapper budgetItemMapper;
    @Mock
    private ApprovalRecordMapper approvalRecordMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AchievementMapper achievementMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private static final Long LEADER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long MANAGER_ID = 99L;

    private Project newProject(ProjectStatus status) {
        Project project = new Project();
        project.setId(10L);
        project.setLeaderId(LEADER_ID);
        project.setProjectName("测试项目");
        project.setStatus(status.name());
        return project;
    }

    @BeforeEach
    void setUp() {
        lenient().when(budgetItemMapper.selectList(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void draft状态下leader提交后应变为submitted() {
        Project project = newProject(ProjectStatus.DRAFT);
        when(projectMapper.selectById(10L)).thenReturn(project);

        projectService.submit(10L, LEADER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.SUBMITTED.name());
    }

    @Test
    void rejected状态下leader重新提交后应变为submitted() {
        Project project = newProject(ProjectStatus.REJECTED);
        when(projectMapper.selectById(10L)).thenReturn(project);

        projectService.submit(10L, LEADER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.SUBMITTED.name());
    }

    @Test
    void submitted状态下manager审批通过后应变为executing() {
        Project project = newProject(ProjectStatus.SUBMITTED);
        when(projectMapper.selectById(10L)).thenReturn(project);
        when(budgetItemMapper.selectList(any())).thenReturn(List.of());

        projectService.approve(10L, new ApproveProjectRequest(), MANAGER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.EXECUTING.name());
    }

    @Test
    void submitted状态下manager驳回后应变为rejected() {
        Project project = newProject(ProjectStatus.SUBMITTED);
        when(projectMapper.selectById(10L)).thenReturn(project);

        RejectRequest request = new RejectRequest();
        request.setComment("预算不合理");
        projectService.reject(10L, request, MANAGER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.REJECTED.name());
    }

    @Test
    void executing状态下leader提交结题后应变为accepting() {
        Project project = newProject(ProjectStatus.EXECUTING);
        when(projectMapper.selectById(10L)).thenReturn(project);

        AcceptanceRequest request = new AcceptanceRequest();
        AchievementRequest achievement = new AchievementRequest();
        achievement.setType(AchievementType.PAPER);
        achievement.setTitle("一篇论文");
        request.setAchievements(List.of(achievement));

        projectService.submitAcceptance(10L, request, LEADER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACCEPTING.name());
    }

    @Test
    void accepting状态下manager验收通过后应变为completed() {
        Project project = newProject(ProjectStatus.ACCEPTING);
        when(projectMapper.selectById(10L)).thenReturn(project);

        projectService.approveAcceptance(10L, MANAGER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED.name());
    }

    @Test
    void accepting状态下manager验收不通过后应退回executing() {
        Project project = newProject(ProjectStatus.ACCEPTING);
        when(projectMapper.selectById(10L)).thenReturn(project);

        RejectRequest request = new RejectRequest();
        request.setComment("成果材料不全");
        projectService.rejectAcceptance(10L, request, MANAGER_ID);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.EXECUTING.name());
    }

    @Test
    void draft状态直接审批通过应被拒绝() {
        Project project = newProject(ProjectStatus.DRAFT);
        when(projectMapper.selectById(10L)).thenReturn(project);

        assertThatThrownBy(() -> projectService.approve(10L, new ApproveProjectRequest(), MANAGER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void completed状态再次提交应被拒绝() {
        Project project = newProject(ProjectStatus.COMPLETED);
        when(projectMapper.selectById(10L)).thenReturn(project);

        assertThatThrownBy(() -> projectService.submit(10L, LEADER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void executing状态不允许重复提交申报() {
        Project project = newProject(ProjectStatus.EXECUTING);
        when(projectMapper.selectById(10L)).thenReturn(project);

        assertThatThrownBy(() -> projectService.submit(10L, LEADER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void draft状态不允许提交结题申请() {
        Project project = newProject(ProjectStatus.DRAFT);
        when(projectMapper.selectById(10L)).thenReturn(project);

        AcceptanceRequest request = new AcceptanceRequest();
        AchievementRequest achievement = new AchievementRequest();
        achievement.setType(AchievementType.PATENT);
        achievement.setTitle("一项专利");
        request.setAchievements(List.of(achievement));

        assertThatThrownBy(() -> projectService.submitAcceptance(10L, request, LEADER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 非leader本人提交应被拒绝() {
        Project project = newProject(ProjectStatus.DRAFT);
        when(projectMapper.selectById(10L)).thenReturn(project);

        assertThatThrownBy(() -> projectService.submit(10L, OTHER_USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 项目不存在时任何操作都应抛出404异常() {
        when(projectMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> projectService.submit(999L, LEADER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(404));
    }
}
