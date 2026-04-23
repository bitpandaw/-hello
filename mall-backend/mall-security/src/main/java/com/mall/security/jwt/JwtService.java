package com.mall.security.jwt;

import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import com.mall.security.prop.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_ACCESS = "acc";
    public static final String TYPE_REFRESH = "ref";
    public static final String CLAIM_KIND = "knd";
    public static final String KIND_MEMBER = "M";
    public static final String KIND_ADMIN = "A";
    public static final String CL_SUB = "sub";
    public static final String CL_JTI = "jti";

    private final JwtProperties props;
    private final StringRedisTemplate stringRedisTemplate;

    private SecretKey key() {
        String s = props.getSecret();
        if (s.length() < 32) {
            s = s + "0".repeat(32 - s.length());
        }
        return Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8));
    }

    public String accessToken(long id, String kind) {
        return build(id, kind, TYPE_ACCESS, props.getAccessExpireMs());
    }

    public String refreshToken(long id, String kind) {
        String jti = UUID.randomUUID().toString();
        long exp = props.getRefreshExpireMs();
        String t = buildWithJti(id, kind, jti, exp);
        String redisKey = refreshKey(kind, id, jti);
        stringRedisTemplate.opsForValue().set(redisKey, "1", exp, TimeUnit.MILLISECONDS);
        return t;
    }

    public Map<String, String> pairForMember(long memberId) {
        return Map.of("access", accessToken(memberId, KIND_MEMBER), "refresh", refreshToken(memberId, KIND_MEMBER));
    }

    public Map<String, String> pairForAdmin(long adminId) {
        return Map.of("access", accessToken(adminId, KIND_ADMIN), "refresh", refreshToken(adminId, KIND_ADMIN));
    }

    public Map<String, String> refresh(String refreshToken) {
        Claims c = parse(refreshToken);
        if (!TYPE_REFRESH.equals(c.get(CLAIM_TYPE))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        String jti = c.getId() != null ? c.getId() : c.get(CL_JTI, String.class);
        long sub = Long.parseLong(c.getSubject());
        String kind = c.get(CLAIM_KIND, String.class);
        String rkey = refreshKey(kind, sub, jti);
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(rkey))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        stringRedisTemplate.delete(rkey);
        if (KIND_MEMBER.equals(kind)) {
            return pairForMember(sub);
        }
        return pairForAdmin(sub);
    }

    public void logoutByRefreshToken(String refreshToken) {
        Claims c = parse(refreshToken);
        if (!TYPE_REFRESH.equals(c.get(CLAIM_TYPE))) {
            return;
        }
        String jti = c.getId() != null ? c.getId() : c.get(CL_JTI, String.class);
        long sub = Long.parseLong(c.getSubject());
        String kind = c.get(CLAIM_KIND, String.class);
        stringRedisTemplate.delete(refreshKey(kind, sub, jti));
    }

    public Claims parseAndValidateAccess(String token) {
        Claims c = parse(token);
        if (!TYPE_ACCESS.equals(c.get(CLAIM_TYPE))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        return c;
    }

    private String refreshKey(String kind, long id, String jti) {
        return "ref:" + kind + ":" + id + ":" + jti;
    }

    private String build(long id, String kind, String typ, long expMs) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(id))
            .claim(CLAIM_TYPE, typ)
            .claim(CLAIM_KIND, kind)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expMs))
            .signWith(key())
            .compact();
    }

    private String buildWithJti(long id, String kind, String jti, long expMs) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(id))
            .id(jti)
            .claim(CLAIM_TYPE, TYPE_REFRESH)
            .claim(CLAIM_KIND, kind)
            .claim(CL_JTI, jti)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expMs))
            .signWith(key())
            .compact();
    }

    public Claims parse(String t) {
        if (t == null || t.isEmpty()) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        String raw = t.startsWith("Bearer ") ? t.substring(7) : t;
        try {
            return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(raw)
                .getPayload();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }
}
