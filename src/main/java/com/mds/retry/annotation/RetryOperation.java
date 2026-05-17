package com.mds.retry.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method or class should be retried upon failure.
 * <p>
 * This annotation can be applied to methods or classes to specify retry behavior using Resilience4j's Retry mechanism.
 * </p>
 *
 * @see io.github.resilience4j.retry.Retry
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryOperation {

  /**
   * The name of the retry instance.
   * <p>
   * If not specified, the default name "default" will be used.
   * </p>
   *
   * @return the name of the retry instance
   */
  String name() default "default";

  /**
   * The fallback class to be used if the method fails.
   * <p>
   * If not specified, the default class will be used.
   * </p>
   *
   * @return the fallback class
   */
  Class<?> fallbackClass() default RetryOperation.class;

  /**
   * The name of the fallback method to be used if the method fails.
   * <p>
   * If not specified, the default method will be used.
   * </p>
   *
   * @return the name of the fallback method
   */
  String fallbackMethod() default "";

}
