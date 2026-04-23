package com.mall.admin.ums.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import com.mall.mbg.entity.UmsMember;
import com.mall.mbg.entity.UmsMemberAddress;
import com.mall.mbg.mapper.UmsMemberAddressMapper;
import com.mall.mbg.mapper.UmsMemberMapper;
import com.mall.security.jwt.JwtService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UmsService {
    private static final String CAP = "captcha:";
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberAddressMapper umsMemberAddressMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;
    private final JwtService jwtService;

    public Map<String, String> captcha() {
        LineCaptcha c = CaptchaUtil.createLineCaptcha(120, 40, 4, 4);
        String id = java.util.UUID.randomUUID().toString();
        String code = c.getCode();
        redis.opsForValue().set(CAP + id, code.toLowerCase(), 2, TimeUnit.MINUTES);
        Map<String, String> m = new HashMap<>();
        m.put("captchaKey", id);
        m.put("captchaImage", c.getImageBase64Data());
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterReq r) {
        String stored = redis.opsForValue().get(CAP + r.getCaptchaKey());
        redis.delete(CAP + r.getCaptchaKey());
        if (!StringUtils.hasText(stored) || !stored.equalsIgnoreCase(r.getCode().trim().toLowerCase())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        if (umsMemberMapper.selectCount(new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getUsername, r.getUsername())) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        if (StringUtils.hasText(r.getPhone()) && umsMemberMapper.selectCount(new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getPhone, r.getPhone())) > 0) {
            throw new BusinessException(ResultCode.PHONE_REGISTERED);
        }
        UmsMember m = new UmsMember();
        m.setUsername(r.getUsername());
        m.setPassword(passwordEncoder.encode(r.getPassword()));
        m.setPhone(r.getPhone());
        m.setEmail(r.getEmail());
        m.setStatus(1);
        umsMemberMapper.insert(m);
    }

    public Map<String, String> login(String username, String password) {
        UmsMember m = umsMemberMapper.selectOne(new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getUsername, username));
        if (m == null || !passwordEncoder.matches(password, m.getPassword()) || m.getStatus() == 0) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        return jwtService.pairForMember(m.getId());
    }

    @Data
    public static class RegisterReq {
        @jakarta.validation.constraints.NotBlank
        private String username;
        @jakarta.validation.constraints.NotBlank
        private String password;
        @jakarta.validation.constraints.NotBlank
        private String captchaKey;
        @jakarta.validation.constraints.NotBlank
        private String code;
        private String phone;
        private String email;
    }

    public UmsMember profile(long memberId) {
        return umsMemberMapper.selectById(memberId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addressSave(long memberId, UmsMemberAddress a) {
        a.setMemberId(memberId);
        if (a.getId() == null) {
            if (Integer.valueOf(1).equals(a.getIsDefault())) {
                clearDefault(memberId);
            }
            umsMemberAddressMapper.insert(a);
        } else {
            a.setMemberId(memberId);
            if (Integer.valueOf(1).equals(a.getIsDefault())) {
                clearDefault(memberId);
            }
            umsMemberAddressMapper.updateById(a);
        }
    }

    private void clearDefault(long memberId) {
        UmsMemberAddress p = new UmsMemberAddress();
        p.setIsDefault(0);
        umsMemberAddressMapper.update(p, new LambdaQueryWrapper<UmsMemberAddress>().eq(UmsMemberAddress::getMemberId, memberId));
    }

    public List<UmsMemberAddress> addressList(long memberId) {
        return umsMemberAddressMapper.selectList(new LambdaQueryWrapper<UmsMemberAddress>().eq(UmsMemberAddress::getMemberId, memberId));
    }

    public void addressDelete(long memberId, long id) {
        umsMemberAddressMapper.delete(new LambdaQueryWrapper<UmsMemberAddress>().eq(UmsMemberAddress::getId, id).eq(UmsMemberAddress::getMemberId, memberId));
    }
}
