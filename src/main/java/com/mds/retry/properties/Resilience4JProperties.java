package com.mds.retry.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Resilience4j Retry.
 * This class maps the properties defined under the prefix "resilience4j.retry" in the application's configuration file.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "resilience4j.retry")
public class Resilience4JProperties {

  /**
   * A map of retry configurations, where the key is the name of the retry instance and the value is the corresponding {@link RetryConfig}.
   */
  private Map<String, RetryConfig> instances = new LinkedHashMap<>();

  /**
   * Inner class representing the configuration for a single retry instance.
   */
  @Setter
  @Getter
  public static class RetryConfig {

    /**
     * The maximum number of retry attempts. Default is 3.
     */
    private int maxAttempts = 3;

    /**
     * The wait duration between retry attempts. Default is 2 seconds.
     */
    private Duration waitDuration = Duration.ofSeconds(2);

    /**
     * A list of exception class names that should trigger a retry.
     */
    private List<String> retryExceptions = new ArrayList<>();

    /**
     * A list of exception class names that should be ignored and not trigger a retry.
     */
    private List<String> ignoreExceptions = new ArrayList<>();
  }
}
