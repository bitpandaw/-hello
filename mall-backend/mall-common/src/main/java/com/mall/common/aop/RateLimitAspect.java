package com.mall.common.aop;

import com.mall.common.annotation.RateLimit;
import com.mall.common.exception.BusinessException;
import com.mall.common.api.ResultCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> LUA = new DefaultRedisScript<>();
    private static final String SCRIPT =
        "local c = redis.call('INCR',KEYS[1]) if c==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end return c";

    static {
        LUA.setScriptText(SCRIPT);
        LUA.setResultType(Long.class);
    }

    @Around("@annotation(r)")
    public Object around(ProceedingJoinPoint pjp, RateLimit r) throws Throwable {
        String id = "anon";
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a && a.getRequest() != null) {
            id = a.getRequest().getRemoteAddr();
        }
        String key = "rl:" + r.key() + ":" + pjp.getSignature().toShortString() + ":" + id;
        List<String> keys = Collections.singletonList(key);
        Long n = stringRedisTemplate.execute(LUA, keys, String.valueOf(r.seconds()));
        if (n != null && n > r.max()) {
            throw new BusinessException(ResultCode.BUSINESS, "操作过于频繁，请稍后再试");
        }
        return pjp.proceed();
    }
}
