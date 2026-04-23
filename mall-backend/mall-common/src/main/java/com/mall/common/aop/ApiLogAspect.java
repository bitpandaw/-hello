package com.mall.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    @Pointcut("execution(* com.mall..controller..*(..))")
    public void web() {
    }

    @Around("web()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.currentTimeMillis();
        String uri = "unknown";
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a) {
            if (a.getRequest() != null) {
                uri = a.getRequest().getRequestURI();
            }
        }
        Object[] args = pjp.getArgs();
        if (log.isInfoEnabled()) {
            log.info("==> request {}.{}() uri={} argsLen={}", pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName(), uri, args == null ? 0 : args.length);
        }
        try {
            Object o = pjp.proceed();
            if (log.isInfoEnabled()) {
                log.info("<== {}.{}() cost={}ms", pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName(), (System.currentTimeMillis() - t0));
            }
            return o;
        } catch (Exception e) {
            log.warn("<== {}.{}() cost={}ms err: {}", pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName(), (System.currentTimeMillis() - t0), e.getMessage());
            throw e;
        }
    }
}
