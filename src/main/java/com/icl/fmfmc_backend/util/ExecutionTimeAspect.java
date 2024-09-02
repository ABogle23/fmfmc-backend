package com.icl.fmfmc_backend.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/** Aspect for logging the execution time of methods annotated with {@link LogExecutionTime}. */
@Aspect
@Component
public class ExecutionTimeAspect {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionTimeAspect.class);

  //    @Pointcut("@annotation(com.icl.fmfmc_backend.util.LogExecutionTime)")
  //    public void annotateExecutionTime() { }

  /**
   * Advice that logs the execution time of methods annotated with {@link LogExecutionTime}.
   *
   * @param joinPoint the join point representing the method execution
   * @param logExecutionTime the annotation containing the log message
   * @return the result of the method execution
   * @throws Throwable if the method execution fails
   */
  @Around("@annotation(logExecutionTime)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime)
      throws Throwable {
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

    logger.info(
        "{} - {} {} ms{}",
        cleanSignature,
        logExecutionTime.message(),
        durationString,
        additionalInfo);
    return result;
  }
}
