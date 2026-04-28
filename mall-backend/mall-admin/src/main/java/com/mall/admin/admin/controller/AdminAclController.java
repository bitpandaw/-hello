package com.mall.admin.admin.controller;

import com.mall.admin.admin.service.AdminAclService;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台-角色权限")
@RestController
@RequestMapping("/admin/acl")
@RequiredArgsConstructor
public class AdminAclController {
    private final AdminAclService adminAclService;

    @GetMapping("/roles")
    @Operation(summary = "角色列表")
    public Result<List<AdminAclService.RoleVO>> roles() {
        return Result.ok(adminAclService.roles());
    }

    @GetMapping("/permissions/tree")
    @Operation(summary = "权限树")
    public Result<List<AdminAclService.PermissionNode>> permissionTree() {
        return Result.ok(adminAclService.permissionTree());
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "角色已授权权限ID列表")
    public Result<List<Long>> rolePermissionIds(@PathVariable long roleId) {
        return Result.ok(adminAclService.rolePermissionIds(roleId));
    }

    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "保存角色权限")
    public Result<?> saveRolePermissions(@PathVariable long roleId, @RequestBody SaveReq req) {
        adminAclService.saveRolePermissions(roleId, req.getPermissionIds());
        return Result.ok();
    }

    @Data
    public static class SaveReq {
        private List<Long> permissionIds;
    }
}

