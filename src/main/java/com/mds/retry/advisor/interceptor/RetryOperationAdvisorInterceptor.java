package com.mds.retry.advisor.interceptor;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import com.mds.retry.advisor.interceptor.exception.MethodInvocationException;
import com.mds.retry.annotation.RetryOperation;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that handles methods annotated with {@link RetryOperation}.
 * It uses Resilience4j's Retry mechanism to retry method executions based on the provided configuration.
 */
public class RetryOperationAdvisorInterceptor implements MethodInterceptor {

  private final RetryRegistry retryRegistry;

  /**
   * Constructor for the interceptor.
   *
   * @param retryRegistry the {@link RetryRegistry} used to manage retry instances.
   */
  public RetryOperationAdvisorInterceptor(RetryRegistry retryRegistry) {
    this.retryRegistry = retryRegistry;
  }

  /**
   * Intercepts the method invocation and applies retry logic if the method is annotated with {@link RetryOperation}.
   *
   * @param invocation the method invocation being intercepted.
   * @return the result of the method execution, potentially retried.
   * @throws Throwable if the method execution or retry logic fails.
   */
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    RetryOperation retryOperation = invocation.getMethod().getAnnotation(RetryOperation.class);
    if (retryOperation == null) return invocation.proceed();

    Retry retry = retryRegistry.retry(retryOperation.name());

    Callable<Object> decoratedCallable = Retry.decorateCallable(retry, () -> validateMethodInvocation(invocation));

    return handlerFallback(invocation, retryOperation, decoratedCallable);
  }

  /**
   * Validates and executes the method invocation.
   *
   * @param invocation the method invocation to validate and execute.
   * @return the result of the method execution.
   * @throws MethodInvocationException if the method execution throws an exception.
   */
  private Object validateMethodInvocation(MethodInvocation invocation) {
    try {
      return invocation.proceed();
    } catch (Throwable ex) {
      throw new MethodInvocationException(ex);
    }
  }

  /**
   * Handles fallback logic if the retries fail.
   *
   * @param invocation        the original method invocation.
   * @param retryOperation    the {@link RetryOperation} annotation containing fallback configuration.
   * @param decoratedCallable the callable wrapped with retry logic.
   * @return the result of the fallback method or rethrows the original exception if no fallback is defined.
   * @throws Exception if the fallback method execution fails.
   */
  private Object handlerFallback(MethodInvocation invocation, RetryOperation retryOperation, Callable<Object> decoratedCallable) throws Exception {
    try {
      return decoratedCallable.call();
    } catch (Throwable throwable) {
      Method fallbackMethod = getFallbackMethod(invocation, retryOperation);
      if (fallbackMethod == null) throw throwable;

      if (!isPublic(fallbackMethod.getModifiers())) {
        makeAccessible(fallbackMethod);
      }
      Object clazz = null;
      if (!isStatic(fallbackMethod.getModifiers())) {
        clazz = generateInstance(fallbackMethod.getDeclaringClass());
      }
      return invokeContingencyMethod(fallbackMethod, clazz);
    }
  }

  /**
   * Retrieves the fallback method to be used in case of a failure during the execution of the original method.
   * <p>
   * This method determines the appropriate fallback method based on the configuration provided in the
   * {@link RetryOperation} annotation. It first checks if a specific fallback class is defined and retrieves
   * the method from that class. If no fallback class is specified, it searches for the fallback method in the
   * class of the intercepted object.
   * </p>
   *
   * @param invocation the original method invocation being intercepted.
   * @param retryOperation the {@link RetryOperation} annotation containing the fallback configuration.
   * @return the {@link Method} object representing the fallback method, or {@code null} if no fallback method is found.
   * @throws NoSuchMethodException if the specified fallback method cannot be found in the fallback class.
   */
  private Method getFallbackMethod(MethodInvocation invocation, RetryOperation retryOperation) {
    String fallbackMethodName = retryOperation.fallbackMethod();
    Method[] methods = (retryOperation.fallbackClass() != RetryOperation.class)
        ? retryOperation.fallbackClass().getDeclaredMethods()
        : Objects.requireNonNull(invocation.getThis()).getClass().getDeclaredMethods();

    return Arrays.stream(methods)
        .filter(method -> method.getName().equals(fallbackMethodName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Generates an instance of the specified class. * <p> * This method creates a new instance of the
   * provided class using its no-argument constructor. * It validates the input class and throws an
   * exception if the class is null or cannot be instantiated. * </p> * * @param <T>   The type of
   * the class to instantiate. * @param clazz The class to instantiate. Must not be null. * @return
   * The generated instance of the specified class. * @throws IllegalArgumentException   If the
   * provided class is null. * @throws ResilienceClassException  If the instantiation fails due to
   * an {@link InstantiationException} or {@link IllegalAccessException}.
   */
  public static <T> T generateInstance(Class<? extends T> clazz) {
    try {
      if (clazz == null) {
        throw new IllegalArgumentException("Clazz not found");
      }
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new MethodInvocationException(e);
    }
  }

  /**
   * Invokes a contingency (fallback) method.
   * <p>
   * This method attempts to execute the provided fallback method on the specified class instance.
   * If an exception occurs during execution, it is encapsulated and thrown as a
   * {@link MethodInvocationException}.
   * </p>
   *
   * @param fallbackMethod The fallback method to invoke. Must not be null.
   * @param clazz          The instance of the class on which the fallback method will be invoked. Must not be null.
   * @return The result of the fallback method execution.
   * @throws MethodInvocationException If any error occurs during the execution of the fallback method.
   */
  private Object invokeContingencyMethod(Method fallbackMethod, Object clazz) {
    try{
      return fallbackMethod.invoke(clazz);
    }catch (Exception exception){
      throw new MethodInvocationException(exception);
    }
  }
}
