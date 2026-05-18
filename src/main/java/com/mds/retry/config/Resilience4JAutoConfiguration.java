package com.mds.retry.config;

import com.mds.retry.properties.Resilience4JProperties;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up Resilience4J Retry instances.
 * <p>
 * This class reads the retry configurations from the application properties
 * and creates a map of {@link Retry} instances that can be used throughout the application.
 * </p>
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({Resilience4JProperties.class})
public class Resilience4JAutoConfiguration {

  private final Resilience4JProperties properties;

  /**
   * Creates a map of {@link Retry} instances based on the configuration properties.
   * <p>
   * This method reads the retry configurations defined in {@link Resilience4JProperties},
   * and for each configuration, it creates a {@link Retry} instance using the specified
   * parameters such as max attempts, wait duration, retry exceptions, and ignored exceptions.
   * </p>
   *
   * @return a map where the key is the name of the retry instance and the value is the {@link Retry} object
   */
  @Bean
  @ConditionalOnMissingBean
  public Map<String, Retry> retries() {
    return properties.getInstances().entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> Retry.of(entry.getKey(), RetryConfig.custom()
                .maxAttempts(entry.getValue().getMaxAttempts())
                .waitDuration(entry.getValue().getWaitDuration())
                .retryExceptions(toExceptionClasses(entry.getValue().getRetryExceptions()))
                .ignoreExceptions(toExceptionClasses(entry.getValue().getIgnoreExceptions()))
                .build()),
            (e1, e2) -> e1,
            ConcurrentHashMap::new
        ));
  }

  /**
   * Converts a list of exception class names to an array of {@code Class<? extends Throwable>}.
   * <p>
   * This method takes a list of fully qualified exception class names and attempts to load
   * their corresponding {@link Class} objects. If the list is null, an empty array is returned.
   * </p>
   *
   * @param exceptionClassNames a list of fully qualified exception class names
   * @return an array of {@link Class} objects representing the exception classes
   */
  private Class<? extends Throwable>[] toExceptionClasses(List<String> exceptionClassNames) {
    if (exceptionClassNames == null || exceptionClassNames.isEmpty()) {
      return new Class[0];
    }
    return exceptionClassNames.stream()
        .map(this::getExceptionClass)
        .toArray(Class[]::new);
  }

  /**
   * Gets the {@link Class} object for the given fully qualified exception class name.
   * <p>
   * This method attempts to load the class using {@link Class#forName(String)}. If the class
   * cannot be found, an {@link IllegalArgumentException} is thrown.
   * </p>
   *
   * @param className the fully qualified name of the exception class
   * @return the {@link Class} object representing the exception class
   * @throws IllegalArgumentException if the class cannot be found
   */
  private Class<? extends Throwable> getExceptionClass(String className) {
    try {
      return Class.forName(className).asSubclass(Throwable.class);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Exception class not found: " + className, e);
    }
  }
}
