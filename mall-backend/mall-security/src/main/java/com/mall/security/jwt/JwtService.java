package com.mall.security.jwt;

import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import com.mall.security.prop.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

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
        return build(id, kind, TYPE_REFRESH, props.getRefreshExpireMs());
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
        long sub = Long.parseLong(c.getSubject());
        String kind = c.get(CLAIM_KIND, String.class);
        if (KIND_MEMBER.equals(kind)) {
            return pairForMember(sub);
        }
        return pairForAdmin(sub);
    }

    public void logoutByRefreshToken(String refreshToken) {
        // Stateless JWT mode (no Redis): logout handled on client side by deleting tokens.
    }

    public Claims parseAndValidateAccess(String token) {
        Claims c = parse(token);
        if (!TYPE_ACCESS.equals(c.get(CLAIM_TYPE))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        return c;
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
