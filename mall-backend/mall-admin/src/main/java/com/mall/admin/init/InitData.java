package com.mall.admin.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.mbg.entity.UmsAdmin;
import com.mall.mbg.entity.UmsMember;
import com.mall.mbg.mapper.UmsAdminMapper;
import com.mall.mbg.mapper.UmsMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@RequiredArgsConstructor
public class InitData implements CommandLineRunner {
    private final UmsAdminMapper umsAdminMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (umsAdminMapper.selectCount(new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, "admin")) == 0) {
            UmsAdmin a = new UmsAdmin();
            a.setUsername("admin");
            a.setPassword(passwordEncoder.encode("123456"));
            a.setStatus(1);
            umsAdminMapper.insert(a);
            Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ums_admin_role WHERE admin_id=?", Long.class, a.getId());
            if (n != null && n == 0) {
                jdbcTemplate.update("INSERT INTO ums_admin_role (admin_id, role_id) VALUES (?,1)", a.getId());
            }
        }
        if (umsMemberMapper.selectCount(new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getUsername, "user01")) == 0) {
            UmsMember m = new UmsMember();
            m.setUsername("user01");
            m.setPassword(passwordEncoder.encode("123456"));
            m.setPhone("13800000000");
            m.setStatus(1);
            umsMemberMapper.insert(m);
        }
    }
}
