package com.mall.admin.ums.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import com.mall.mbg.entity.UmsMember;
import com.mall.mbg.entity.UmsMemberAddress;
import com.mall.mbg.mapper.UmsMemberAddressMapper;
import com.mall.mbg.mapper.UmsMemberMapper;
import com.mall.security.jwt.JwtService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UmsService {
    private static final long CAPTCHA_TTL_MS = 2 * 60 * 1000L;

    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberAddressMapper umsMemberAddressMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Map<String, CaptchaEntry> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    @Data
    private static class CaptchaEntry {
        private final String code;
        private final long expireAtMs;
    }

    public Map<String, String> captcha() {
        LineCaptcha c = CaptchaUtil.createLineCaptcha(120, 40, 4, 4);
        String id = java.util.UUID.randomUUID().toString();
        String code = c.getCode();
        CAPTCHA_CACHE.put(id, new CaptchaEntry(code.toLowerCase(), System.currentTimeMillis() + CAPTCHA_TTL_MS));
        Map<String, String> m = new HashMap<>();
        m.put("captchaKey", id);
        m.put("captchaImage", c.getImageBase64Data());
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> register(RegisterReq r) {
        CaptchaEntry entry = CAPTCHA_CACHE.remove(r.getCaptchaKey());
        if (entry == null || entry.getExpireAtMs() < System.currentTimeMillis()) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        String stored = entry.getCode();
        if (!StringUtils.hasText(stored)) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        String inputCode = r.getCode() == null ? "" : r.getCode().trim().toLowerCase();
        if (!stored.equalsIgnoreCase(inputCode)) {
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
        return jwtService.pairForMember(m.getId());
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
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String captchaKey;
        @NotBlank
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
