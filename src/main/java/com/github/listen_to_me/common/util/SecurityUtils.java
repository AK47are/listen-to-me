package com.github.listen_to_me.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.listen_to_me.common.exception.AuthException;

public class SecurityUtils {
    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 判断认证对象非空 + 已认证
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("认证获取失败");
        }

        return (Long) authentication.getPrincipal();
    }
}
