package com.researchease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.researchease.common.BusinessException;
import com.researchease.dto.UserCreateRequest;
import com.researchease.dto.UserResponse;
import com.researchease.entity.ApprovalRecord;
import com.researchease.entity.Expenditure;
import com.researchease.entity.Project;
import com.researchease.entity.ProgressReport;
import com.researchease.entity.User;
import com.researchease.mapper.ApprovalRecordMapper;
import com.researchease.mapper.ExpenditureMapper;
import com.researchease.mapper.ProgressReportMapper;
import com.researchease.mapper.ProjectMapper;
import com.researchease.mapper.UserMapper;
import com.researchease.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Set<String> PROTECTED_USERNAMES = Set.of("admin", "manager", "researcher");

    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final ExpenditureMapper expenditureMapper;
    private final ProgressReportMapper progressReportMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> list() {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt)
        ).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        Long existing = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing != null && existing > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole().name());
        user.setEnabled(true);
        userMapper.insert(user);
        return toResponse(user);
    }

    @Override
    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setEnabled(enabled);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (PROTECTED_USERNAMES.contains(user.getUsername())) {
            throw new BusinessException("该账号为演示保留账号，不可删除");
        }
        boolean hasReferences = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getLeaderId, userId)) > 0
                || expenditureMapper.selectCount(
                new LambdaQueryWrapper<Expenditure>().eq(Expenditure::getCreatedBy, userId)) > 0
                || progressReportMapper.selectCount(
                new LambdaQueryWrapper<ProgressReport>().eq(ProgressReport::getCreatedBy, userId)) > 0
                || approvalRecordMapper.selectCount(
                new LambdaQueryWrapper<ApprovalRecord>().eq(ApprovalRecord::getOperatorId, userId)) > 0;
        if (hasReferences) {
            throw new BusinessException("该账号已关联项目、经费或审批记录，无法删除，可改为禁用账号");
        }
        userMapper.deleteById(userId);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRealName(), user.getRole(),
                user.getEnabled(), user.getCreatedAt());
    }
}
