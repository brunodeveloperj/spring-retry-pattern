package com.mds.retry.advisor.configuration;

import com.mds.retry.advisor.interceptor.RetryOperationAdvisorInterceptor;
import com.mds.retry.annotation.RetryOperation;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration class for setting up the RetryOperationAdvisor.
 * <p>
 * This class configures an advisor that intercepts method calls annotated with
 * {@link RetryOperation} and applies retry logic using Resilience4j.
 * </p>
 */
@Configurable
@ComponentScan("com.mds.retry")
public class RetryOperationAdvisorConfiguration {

  /**
   * Creates a Spring AOP advisor that intercepts methods annotated with {@link RetryOperation}.
   * <p>
   * This method sets up an AspectJ pointcut to match the annotation and associates it with the
   * {@link RetryOperationAdvisorInterceptor} for handling retry logic.
   * </p>
   *
   * @param retryRegistry the {@link RetryRegistry} used to manage retry instances.
   * @return an {@link Advisor} configured for retry operations.
   */
  @Bean
  public Advisor retryOperationAdvisor(RetryRegistry retryRegistry) {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression("@annotation(com.mds.retry.annotation.RetryOperation)");
    return new DefaultPointcutAdvisor(pointcut, new RetryOperationAdvisorInterceptor(retryRegistry));
  }

}
