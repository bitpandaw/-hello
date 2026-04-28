package com.mall.common.aop;

import com.mall.common.annotation.RateLimit;
import com.mall.common.exception.BusinessException;
import com.mall.common.api.ResultCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {
    private static final Map<String, Counter> COUNTERS = new ConcurrentHashMap<>();

    private static final class Counter {
        private long count;
        private long windowStartMs;
    }

    @Around("@annotation(r)")
    public Object around(ProceedingJoinPoint pjp, RateLimit r) throws Throwable {
        String id = "anon";
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a && a.getRequest() != null) {
            id = a.getRequest().getRemoteAddr();
        }
        String key = "rl:" + r.key() + ":" + pjp.getSignature().toShortString() + ":" + id;
        long now = System.currentTimeMillis();
        long windowMs = r.seconds() * 1000L;
        Counter c = COUNTERS.computeIfAbsent(key, k -> {
            Counter x = new Counter();
            x.windowStartMs = now;
            return x;
        });
        synchronized (c) {
            if (now - c.windowStartMs >= windowMs) {
                c.windowStartMs = now;
                c.count = 0;
            }
            c.count++;
            if (c.count > r.max()) {
                throw new BusinessException(ResultCode.BUSINESS, "操作过于频繁，请稍后再试");
            }
        }
        if (COUNTERS.size() > 10_000) {
            COUNTERS.entrySet().removeIf(e -> now - e.getValue().windowStartMs > 10 * 60 * 1000L);
        }
        return pjp.proceed();
    }
}
