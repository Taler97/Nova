package com.nova.aspect;

import com.nova.annotation.AutoFill;
import com.nova.context.BaseContext;
import com.nova.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.nova.mapper.*.*(..)) && @annotation(com.nova.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType type = autoFill.value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return;
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if (type == OperationType.INSERT) {
            try {
                entity.getClass().getMethod("setCreateTime", LocalDateTime.class).invoke(entity, now);
                entity.getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(entity, now);
                entity.getClass().getMethod("setCreateUser", Long.class).invoke(entity, currentId);
                entity.getClass().getMethod("setUpdateUser", Long.class).invoke(entity, currentId);
            } catch (Exception e) {
                log.error("AutoFill INSERT error", e);
            }
        } else if (type == OperationType.UPDATE) {
            try {
                entity.getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(entity, now);
                entity.getClass().getMethod("setUpdateUser", Long.class).invoke(entity, currentId);
            } catch (Exception e) {
                log.error("AutoFill UPDATE error", e);
            }
        }
    }
}
