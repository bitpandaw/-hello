package com.mall.admin.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.security.SecurityUser;
import com.mall.mbg.entity.UmsPermission;
import com.mall.mbg.mapper.UmsPermissionMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMenuService {
    private final UmsPermissionMapper permissionMapper;
    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class MenuNode {
        private Long id;
        private String name;
        private String path;
        private String component;
        private String code;
        private List<MenuNode> children;
    }

    public List<MenuNode> menuTree() {
        long adminId = SecurityUser.requireAdmin();
        List<Long> roleIds = jdbcTemplate.query("SELECT role_id FROM ums_admin_role WHERE admin_id=?", (rs, i) -> rs.getLong(1), adminId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        String in = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<UmsPermission> all = permissionMapper.selectList(new LambdaQueryWrapper<UmsPermission>()
            .inSql(UmsPermission::getId, "SELECT permission_id FROM ums_role_permission WHERE role_id IN (" + in + ")")
            .eq(UmsPermission::getDeleted, 0)
            .in(UmsPermission::getType, 0, 1)
            .orderByAsc(UmsPermission::getParentId, UmsPermission::getSort));
        Map<Long, List<UmsPermission>> byParent = all.stream()
            .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));
        return build(byParent, 0L);
    }

    private List<MenuNode> build(Map<Long, List<UmsPermission>> byParent, long pid) {
        List<UmsPermission> list = byParent.getOrDefault(pid, List.of());
        List<MenuNode> out = new ArrayList<>();
        for (UmsPermission p : list) {
            MenuNode n = new MenuNode();
            n.setId(p.getId());
            n.setName(p.getName());
            n.setPath(p.getPath());
            n.setComponent(p.getComponent());
            n.setCode(p.getCode());
            n.setChildren(build(byParent, p.getId()));
            out.add(n);
        }
        return out;
    }
}
