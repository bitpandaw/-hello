package com.mall.admin.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.Result;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import com.mall.mbg.entity.UmsAdmin;
import com.mall.mbg.mapper.UmsAdminMapper;
import com.mall.security.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-认证")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final UmsAdminMapper umsAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<?> login(@RequestBody @jakarta.validation.Valid L r) {
        UmsAdmin a = umsAdminMapper.selectOne(new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, r.getUsername()));
        if (a == null || a.getStatus() == 0 || !passwordEncoder.matches(r.getPassword(), a.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        return Result.ok(jwtService.pairForAdmin(a.getId()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新(Refresh Token)")
    public Result<?> ref(@RequestHeader("Authorization") String t) {
        return Result.ok(jwtService.refresh(t));
    }

    @Data
    public static class L {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
}
