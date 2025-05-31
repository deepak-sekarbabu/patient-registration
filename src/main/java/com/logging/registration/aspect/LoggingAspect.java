package com.logging.registration.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.registration.service..*(..)) || " +
            "execution(* com.example.registration.controller..*(..)) || " +
            "execution(* com.example.registration.repository..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        Object[] methodArgs = joinPoint.getArgs();

        logger.info("Enter: {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(methodArgs));

        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.info("Exit: {}.{}() with result = {}. Execution time = {} ms",
                    className, methodName, result, (endTime - startTime));
            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            logger.error("Exception in {}.{}() with cause = '{}' and exception = '{}'. Execution time = {} ms",
                    className, methodName, throwable.getCause() != null ? throwable.getCause() : "NULL",
                    throwable.getMessage(), (endTime - startTime));
            throw throwable;
        }
    }
}
