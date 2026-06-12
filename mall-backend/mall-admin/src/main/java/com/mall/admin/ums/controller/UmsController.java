package com.mall.admin.ums.controller;

import com.mall.admin.security.SecurityUser;
import com.mall.admin.ums.service.UmsService;
import com.mall.common.annotation.RateLimit;
import com.mall.common.api.Result;
import com.mall.common.json.Sensitive;
import com.mall.mbg.entity.UmsMember;
import com.mall.mbg.entity.UmsMemberAddress;
import com.mall.security.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户")
@RestController
@RequestMapping("/api/ums")
@RequiredArgsConstructor
public class UmsController {
    private final UmsService umsService;
    private final JwtService jwtService;

    @GetMapping("/captcha")
    @Operation(summary = "图形验证码")
    public Result<?> captcha() {
        return Result.ok(umsService.captcha());
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    @RateLimit(key = "reg", max = 10, seconds = 60)
    public Result<?> register(@RequestBody @Valid UmsService.RegisterReq req) {
        return Result.ok(umsService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<?> login(@RequestBody @Valid LoginReq r) {
        return Result.ok(umsService.login(r.getUsername(), r.getPassword()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<?> refresh(@RequestHeader("Authorization") String refreshToken) {
        return Result.ok(jwtService.refresh(refreshToken));
    }

    @GetMapping("/me")
    @Operation(summary = "当前用户")
    public Result<MemberProfileVO> me() {
        UmsMember m = umsService.profile(SecurityUser.requireMember());
        MemberProfileVO vo = new MemberProfileVO();
        vo.setUsername(m.getUsername());
        vo.setPhone(m.getPhone());
        vo.setEmail(m.getEmail());
        vo.setAvatar(m.getAvatar());
        return Result.ok(vo);
    }

    @Data
    public static class MemberProfileVO {
        private String username;
        @Sensitive
        private String phone;
        private String email;
        private String avatar;
    }

    @Data
    public static class LoginReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @GetMapping("/address")
    @Operation(summary = "地址列表")
    public Result<List<UmsMemberAddress>> addrList() {
        return Result.ok(umsService.addressList(SecurityUser.requireMember()));
    }

    @PostMapping("/address")
    @Operation(summary = "新增/修改地址")
    public Result<?> addr(@RequestBody UmsMemberAddress a) {
        umsService.addressSave(SecurityUser.requireMember(), a);
        return Result.ok();
    }

    @DeleteMapping("/address/{id}")
    @Operation(summary = "删除地址")
    public Result<?> addrDel(@PathVariable long id) {
        umsService.addressDelete(SecurityUser.requireMember(), id);
        return Result.ok();
    }
}
