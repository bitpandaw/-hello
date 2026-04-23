package com.mall.admin.admin.controller;

import com.mall.admin.admin.service.AdminMenuService;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台-动态菜单")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMenuController {
    private final AdminMenuService adminMenuService;

    @GetMapping("/menus")
    @Operation(summary = "当前管理员可见菜单树(RBAC)")
    public Result<java.util.List<AdminMenuService.MenuNode>> menus() {
        return Result.ok(adminMenuService.menuTree());
    }
}
