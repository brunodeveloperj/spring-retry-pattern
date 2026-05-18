package com.mds.retry.advisor.interceptor.exception;

/**
 * Exception class representing an error that occurs during method invocation.
 * This exception is used to wrap any throwable encountered while invoking a method.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public class MethodInvocationException extends RuntimeException {

  /**
   * Constructs a new MethodInvocationException with the specified cause.
   *
   * @param throwable the cause of the exception, typically the original exception thrown during method invocation.
   */
  public MethodInvocationException(Throwable throwable) {
    super(throwable);
  }
}
