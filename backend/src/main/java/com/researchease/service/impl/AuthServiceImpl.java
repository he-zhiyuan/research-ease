package com.researchease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.researchease.common.BusinessException;
import com.researchease.common.JwtUtil;
import com.researchease.dto.LoginRequest;
import com.researchease.dto.LoginResponse;
import com.researchease.entity.User;
import com.researchease.mapper.UserMapper;
import com.researchease.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }
}
