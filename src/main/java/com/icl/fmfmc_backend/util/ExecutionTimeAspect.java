package com.icl.fmfmc_backend.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionTimeAspect.class);

//    @Pointcut("@annotation(com.icl.fmfmc_backend.util.LogExecutionTime)")
//    public void annotateExecutionTime() { }

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        Double duration = (endTime - startTime) / 1000.0; // to secs
        String durationString = String.format("%.2f", duration);

        String signature = joinPoint.getSignature().toString();
        String cleanSignature = signature.replace("com.icl.fmfmc_backend.", "");
        String additionalInfo = "";

        if (result instanceof List) {
            int size = ((List<?>) result).size();
            additionalInfo = String.format(" (size: %d results) ", size);
        }

        logger.info("{} - {} {} ms{}", cleanSignature, logExecutionTime.message(), durationString, additionalInfo);
        return result;
    }
}