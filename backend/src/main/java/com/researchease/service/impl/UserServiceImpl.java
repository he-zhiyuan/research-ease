package com.researchease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.researchease.common.BusinessException;
import com.researchease.dto.UserCreateRequest;
import com.researchease.dto.UserResponse;
import com.researchease.entity.User;
import com.researchease.mapper.UserMapper;
import com.researchease.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
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

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRealName(), user.getRole(),
                user.getEnabled(), user.getCreatedAt());
    }
}
