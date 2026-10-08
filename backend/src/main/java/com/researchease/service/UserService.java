package com.researchease.service;

import com.researchease.dto.UserCreateRequest;
import com.researchease.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> list();

    UserResponse create(UserCreateRequest request);

    void setEnabled(Long userId, boolean enabled);
}
