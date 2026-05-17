# Scaffold — spring-retry-pattern

> Use este documento para solicitar a uma IA que recrie este projeto do zero, exatamente como está estruturado aqui.
> Copie e cole o bloco de prompt abaixo diretamente para a IA.

---

## Prompt para IA

```
Crie um projeto Maven chamado spring-retry-pattern com a seguinte estrutura e conteúdo exato de cada arquivo.

### Metadados do projeto

- GroupId:    com.mds.retry
- ArtifactId: spring-retry-pattern
- Version:    0.0.1-SNAPSHOT
- Packaging:  jar
- Java:       21
- Spring Boot: 3.4.0

### Estrutura de pastas

spring-retry-pattern/
├── lombok.config
├── pom.xml
├── doc/
│   └── scaffold.md          ← este arquivo
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mds/retry/
│   │   │       ├── annotation/
│   │   │       │   └── RetryOperation.java
│   │   │       ├── properties/
│   │   │       │   └── Resilience4JProperties.java
│   │   │       ├── config/
│   │   │       │   └── Resilience4JAutoConfiguration.java
│   │   │       └── advisor/
│   │   │           ├── configuration/
│   │   │           │   └── RetryOperationAdvisorConfiguration.java
│   │   │           └── interceptor/
│   │   │               ├── RetryOperationAdvisorInterceptor.java
│   │   │               └── exception/
│   │   │                   └── MethodInvocationException.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── spring/
│   │               └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
│       └── java/
│           └── com/mds/retry/
│               (vazio por enquanto)

### Regras de estilo (Google Style)

- Indentação Java:  2 espaços
- Indentação XML:   2 espaços
- Imports:          sem wildcard, ordem: módulo → static → linha vazia → externos
- Chaves:           sempre obrigatórias em if/for/while/do-while
- Operadores binários/ternários: na linha seguinte ao quebrar

---

### Arquivo: lombok.config

config.stopBubbling = true
lombok.addLombokGeneratedAnnotation = true
lombok.toString.doNotUseGetters = true
lombok.equalsAndHashCode.doNotUseGetters = true

---

### Arquivo: pom.xml

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.0</version>
    <relativePath/>
  </parent>

  <groupId>com.mds.retry</groupId>
  <artifactId>spring-retry-pattern</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>jar</packaging>
  <name>spring-retry-pattern</name>
  <description>MDS Spring Retry Pattern Library</description>

  <properties>
    <!-- Java compile version -->
    <java.version>21</java.version>

    <!-->External Properties<-->
    <resilience4j-spring-boot3.version>2.3.0</resilience4j-spring-boot3.version>

    <!-->Plugins<-->
    <fmt-maven-plugin.version>2.13</fmt-maven-plugin.version>
  </properties>

  <dependencies>
    <!-- Spring Boot Libraries -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>

    <!-- External Libraries -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-spring-boot3</artifactId>
      <version>${resilience4j-spring-boot3.version}</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
      </plugin>

      <plugin>
        <groupId>com.coveo</groupId>
        <artifactId>fmt-maven-plugin</artifactId>
        <version>${fmt-maven-plugin.version}</version>
        <configuration>
          <skip>true</skip>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>format</goal>
            </goals>
            <phase>process-sources</phase>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

</project>

---

### Arquivo: com/mds/retry/annotation/RetryOperation.java

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

---

### Arquivo: com/mds/retry/properties/Resilience4JProperties.java

package com.mds.retry.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Resilience4j Retry.
 * This class maps the properties defined under the prefix "resilience4j.retry" in the application's configuration file.
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "resilience4j.retry")
public class Resilience4JProperties {

  /**
   * A map of retry configurations, where the key is the name of the retry instance
   * and the value is the corresponding {@link RetryConfig}.
   */
  private Map<String, RetryConfig> instances;

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

---

### Arquivo: com/mds/retry/config/Resilience4JAutoConfiguration.java

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
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({Resilience4JProperties.class})
public class Resilience4JAutoConfiguration {

  private final Resilience4JProperties properties;

  /**
   * Creates a map of {@link Retry} instances based on the configuration properties.
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

  private Class<? extends Throwable>[] toExceptionClasses(List<String> exceptionClassNames) {
    if (exceptionClassNames == null || exceptionClassNames.isEmpty()) {
      return new Class[0];
    }
    return exceptionClassNames.stream()
        .map(this::getExceptionClass)
        .toArray(Class[]::new);
  }

  private Class<? extends Throwable> getExceptionClass(String className) {
    try {
      return Class.forName(className).asSubclass(Throwable.class);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Exception class not found: " + className, e);
    }
  }
}

---

### Arquivo: com/mds/retry/advisor/configuration/RetryOperationAdvisorConfiguration.java

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

---

### Arquivo: com/mds/retry/advisor/interceptor/RetryOperationAdvisorInterceptor.java

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
   * Intercepts the method invocation and applies retry logic if the method is annotated
   * with {@link RetryOperation}.
   *
   * @param invocation the method invocation being intercepted.
   * @return the result of the method execution, potentially retried.
   * @throws Throwable if the method execution or retry logic fails.
   */
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    RetryOperation retryOperation = invocation.getMethod().getAnnotation(RetryOperation.class);
    if (retryOperation == null) {
      return invocation.proceed();
    }

    Retry retry = retryRegistry.retry(retryOperation.name());
    Callable<Object> decoratedCallable =
        Retry.decorateCallable(retry, () -> validateMethodInvocation(invocation));

    return handlerFallback(invocation, retryOperation, decoratedCallable);
  }

  private Object validateMethodInvocation(MethodInvocation invocation) {
    try {
      return invocation.proceed();
    } catch (Throwable ex) {
      throw new MethodInvocationException(ex);
    }
  }

  private Object handlerFallback(
      MethodInvocation invocation,
      RetryOperation retryOperation,
      Callable<Object> decoratedCallable)
      throws Exception {
    try {
      return decoratedCallable.call();
    } catch (Throwable throwable) {
      Method fallbackMethod = getFallbackMethod(invocation, retryOperation);
      if (fallbackMethod == null) {
        throw throwable;
      }

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

  private Method getFallbackMethod(MethodInvocation invocation, RetryOperation retryOperation) {
    String fallbackMethodName = retryOperation.fallbackMethod();
    Method[] methods =
        (retryOperation.fallbackClass() != RetryOperation.class)
            ? retryOperation.fallbackClass().getDeclaredMethods()
            : Objects.requireNonNull(invocation.getThis()).getClass().getDeclaredMethods();

    return Arrays.stream(methods)
        .filter(method -> method.getName().equals(fallbackMethodName))
        .findFirst()
        .orElse(null);
  }

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

  private Object invokeContingencyMethod(Method fallbackMethod, Object clazz) {
    try {
      return fallbackMethod.invoke(clazz);
    } catch (Exception exception) {
      throw new MethodInvocationException(exception);
    }
  }
}

---

### Arquivo: com/mds/retry/advisor/interceptor/exception/MethodInvocationException.java

package com.mds.retry.advisor.interceptor.exception;

/**
 * Exception class representing an error that occurs during method invocation.
 * This exception is used to wrap any throwable encountered while invoking a method.
 */
public class MethodInvocationException extends RuntimeException {

  /**
   * Constructs a new MethodInvocationException with the specified cause.
   *
   * @param throwable the cause of the exception.
   */
  public MethodInvocationException(Throwable throwable) {
    super(throwable);
  }
}

---

### Arquivo: src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

com.mds.retry.advisor.configuration.RetryOperationAdvisorConfiguration

---

Crie todos os arquivos acima respeitando exatamente: pacotes, nomes de classes, anotações, imports e indentação de 2 espaços (Google Style).
```
