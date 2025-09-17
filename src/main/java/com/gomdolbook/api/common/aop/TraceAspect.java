package com.gomdolbook.api.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TraceAspect {

    @Pointcut("@within(com.gomdolbook.api.common.config.annotations.DomainService)")
    private void allService() {}

    @Pointcut("@within(com.gomdolbook.api.common.config.annotations.DomainRepository)")
    private void allRepository() {}

    @Around("allService()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}, args: {}", joinPoint.getSignature(), joinPoint.getArgs());
        return joinPoint.proceed();
    }

    @Around("allRepository()")
    public Object doDbAccessLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            long time = end - start;
            log.info("[log] {}, running time(ms) : {}", joinPoint.getSignature(), time);
        }
    }
}
