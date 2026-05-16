# spring-retry-pattern

A reusable Spring Boot library that centralizes and standardizes retry logic using Spring Retry and Resilience4j.

## Features

- Retry mechanism
- Exponential backoff
- Fallback support
- Circuit breaker
- Timeout
- Configurable policies
- Annotation-driven retries

---

## Example

```java
@Retryable(
    retryFor = Exception.class,
    maxAttempts = 3
)
public User findUser(){
}
```

---

## Architecture

Application
↓
Retry Layer
↓
Resilience4J
↓
External Service
