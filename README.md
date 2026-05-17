# Spring Retry Pattern

Reusable Spring Boot retry library built on top of Resilience4J with annotation-driven execution, fallback strategies and auto-configuration support.

## Overview

This project provides a reusable retry module for Spring Boot applications, allowing retry behavior through a custom annotation and centralized configuration.

Main goals:

* Reduce duplicated retry logic across services
* Standardize retry implementation
* Provide fallback support
* Enable configuration-driven retry strategies
* Offer plug-and-play integration for microservices

---

## Features

✅ Custom `@RetryOperation` annotation
✅ Resilience4J integration
✅ Spring Boot auto-configuration
✅ AOP interception
✅ Dynamic retry properties
✅ Fallback support
✅ Exception abstraction layer
✅ Modular architecture

---

## Tech Stack

* Java 21
* Spring Boot 3.4.0
* Spring AOP
* Maven
* Resilience4J
* Lombok

---

## Installation

Add dependency:

```xml
<dependency>
    <groupId>com.mds.retry</groupId>
    <artifactId>spring-retry-pattern</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

## Configuration

Example:

```yaml
resilience4j:
  retry:
    instances:
      customerRetry:
        maxAttempts: 3
        waitDuration: 2s
        retryExceptions:
          - java.net.SocketTimeoutException
          - java.io.IOException
        ignoreExceptions:
          - java.lang.IllegalArgumentException
```

---

## Usage

### Basic retry

```java
@RetryOperation(name = "customerRetry")
public String execute() {
   return externalService.call();
}
```

---

### Retry with fallback

```java
@RetryOperation(
    name = "customerRetry",
    fallbackMethod = "fallback"
)
public String execute() {
   return externalService.call();
}

public String fallback() {
   return "Fallback response";
}
```

---

## Project Structure

```text
spring-retry-pattern/
├── annotation/
├── advisor/
│   ├── configuration/
│   └── interceptor/
├── config/
├── properties/
└── resources/
```

---

## Architecture Flow

```text
@RetryOperation
        ↓
AOP Advisor
        ↓
Interceptor
        ↓
RetryRegistry
        ↓
Resilience4J Retry
        ↓
Fallback (optional)
```

---

## Roadmap

* [ ] Unit tests
* [ ] Integration tests
* [ ] Metrics support
* [ ] Logging strategy
* [ ] Retry listeners/events
* [ ] Custom fallback parameters

---

## Author

Martins Desenvolvimento de Sistemas
