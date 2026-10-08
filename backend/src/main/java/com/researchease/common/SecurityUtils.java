package com.researchease.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return loginUser;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static String getCurrentRole() {
        return getCurrentUser().getRole();
    }
}
