package com.mall.admin.security;

import com.mall.security.user.MallPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUser {
    public static long requireMember() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || !(a.getPrincipal() instanceof MallPrincipal p)) {
            throw new com.mall.common.exception.BusinessException(com.mall.common.api.ResultCode.UNAUTHORIZED, "需要登录");
        }
        if (!"M".equals(p.getKind())) {
            throw new com.mall.common.exception.BusinessException(com.mall.common.api.ResultCode.FORBIDDEN, "需要用户身份");
        }
        return p.getId();
    }

    public static long requireAdmin() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || !(a.getPrincipal() instanceof MallPrincipal p)) {
            throw new com.mall.common.exception.BusinessException(com.mall.common.api.ResultCode.UNAUTHORIZED, "需要登录");
        }
        if (!"A".equals(p.getKind())) {
            throw new com.mall.common.exception.BusinessException(com.mall.common.api.ResultCode.FORBIDDEN, "需要管理员");
        }
        return p.getId();
    }
}
