package com.researchease.service;

import com.researchease.dto.LoginRequest;
import com.researchease.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
