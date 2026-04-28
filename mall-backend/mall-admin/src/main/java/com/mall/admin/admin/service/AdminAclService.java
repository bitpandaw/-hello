package com.mall.admin.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.security.SecurityUser;
import com.mall.mbg.entity.UmsPermission;
import com.mall.mbg.entity.UmsRole;
import com.mall.mbg.mapper.UmsPermissionMapper;
import com.mall.mbg.mapper.UmsRoleMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAclService {
    private final UmsRoleMapper roleMapper;
    private final UmsPermissionMapper permissionMapper;
    private final JdbcTemplate jdbcTemplate;

    @Data
    public static class RoleVO {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    public static class PermissionNode {
        private Long id;
        private String name;
        private String code;
        private String path;
        private String component;
        private Integer type;
        private List<PermissionNode> children;
    }

    public List<RoleVO> roles() {
        SecurityUser.requireAdmin();
        return roleMapper.selectList(new LambdaQueryWrapper<UmsRole>()
                .eq(UmsRole::getDeleted, 0)
                .orderByAsc(UmsRole::getId))
            .stream()
            .map(r -> {
                RoleVO v = new RoleVO();
                v.setId(r.getId());
                v.setName(r.getName());
                v.setCode(r.getCode());
                return v;
            })
            .collect(Collectors.toList());
    }

    public List<PermissionNode> permissionTree() {
        SecurityUser.requireAdmin();
        List<UmsPermission> all = permissionMapper.selectList(new LambdaQueryWrapper<UmsPermission>()
            .eq(UmsPermission::getDeleted, 0)
            .orderByAsc(UmsPermission::getParentId, UmsPermission::getSort, UmsPermission::getId));
        Map<Long, List<UmsPermission>> byParent = all.stream()
            .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));
        return buildTree(byParent, 0L);
    }

    public List<Long> rolePermissionIds(long roleId) {
        SecurityUser.requireAdmin();
        return jdbcTemplate.query(
            "SELECT permission_id FROM ums_role_permission WHERE role_id=?",
            (rs, i) -> rs.getLong(1),
            roleId
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissions(long roleId, List<Long> permissionIds) {
        SecurityUser.requireAdmin();
        jdbcTemplate.update("DELETE FROM ums_role_permission WHERE role_id=?", roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        Set<Long> distinct = new HashSet<>(permissionIds);
        for (Long pid : distinct) {
            if (pid == null) {
                continue;
            }
            jdbcTemplate.update("INSERT INTO ums_role_permission(role_id, permission_id) VALUES (?, ?)", roleId, pid);
        }
    }

    private List<PermissionNode> buildTree(Map<Long, List<UmsPermission>> byParent, long parentId) {
        List<UmsPermission> list = byParent.getOrDefault(parentId, List.of());
        List<PermissionNode> out = new ArrayList<>();
        for (UmsPermission p : list) {
            PermissionNode n = new PermissionNode();
            n.setId(p.getId());
            n.setName(p.getName());
            n.setCode(p.getCode());
            n.setPath(p.getPath());
            n.setComponent(p.getComponent());
            n.setType(p.getType());
            n.setChildren(buildTree(byParent, p.getId()));
            out.add(n);
        }
        return out;
    }
}

