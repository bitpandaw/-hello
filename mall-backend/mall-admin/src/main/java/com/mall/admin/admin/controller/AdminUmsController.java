package com.mall.admin.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.admin.admin.service.AdminUmsService;
import com.mall.common.api.Result;
import com.mall.mbg.entity.UmsMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-用户")
@RestController
@RequestMapping("/admin/ums/members")
@RequiredArgsConstructor
public class AdminUmsController {
    private final AdminUmsService adminUmsService;

    @GetMapping
    @Operation(summary = "C端用户列表")
    public Result<IPage<UmsMember>> list(@RequestParam(defaultValue = "1") int p, @RequestParam(defaultValue = "10") int s, @RequestParam(required = false) String username) {
        return Result.ok(adminUmsService.page(p, s, username));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "封禁/解封 status=0封禁,1正常")
    public Result<?> st(@PathVariable long id, @RequestBody S r) {
        adminUmsService.setStatus(id, r.getStatus());
        return Result.ok();
    }

    @Data
    public static class S {
        private int status;
    }
}
