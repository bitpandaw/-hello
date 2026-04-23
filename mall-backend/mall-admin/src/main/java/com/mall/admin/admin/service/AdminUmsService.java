package com.mall.admin.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.security.SecurityUser;
import com.mall.mbg.entity.UmsMember;
import com.mall.mbg.mapper.UmsMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUmsService {
    private final UmsMemberMapper umsMemberMapper;

    public IPage<UmsMember> page(int p, int s, String username) {
        SecurityUser.requireAdmin();
        return umsMemberMapper.selectPage(new Page<>(p, s), new LambdaQueryWrapper<UmsMember>()
            .like(username != null && !username.isBlank(), UmsMember::getUsername, username)
            .orderByDesc(UmsMember::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void setStatus(long id, int st) {
        SecurityUser.requireAdmin();
        UmsMember m = umsMemberMapper.selectById(id);
        if (m == null) {
            return;
        }
        m.setStatus(st);
        umsMemberMapper.updateById(m);
    }
}
