package com.icl.fmfmc_backend.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to log the execution time of a method.
 *
 * <p>This annotation can be applied to methods to log their execution time. The default log message
 * is "Executed in " but can be customized using the {@code message} attribute.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
  String message() default "Executed in ";
}
