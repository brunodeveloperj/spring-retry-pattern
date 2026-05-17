# spring-retry-pattern — Playbook

Biblioteca reutilizável Spring Boot que centraliza e padroniza lógica de retry via **Resilience4j** com suporte a anotações, fallback e configuração externalizada.

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.0 |
| Resilience4j | 2.3.0 |
| Lombok | (gerenciado pelo Spring Boot) |

---

## Estrutura do Projeto

```
spring-retry-pattern/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/mds/retry/
                ├── annotation/
                │   └── RetryOperation.java
                ├── properties/
                │   └── Resilience4JProperties.java
                ├── config/
                │   └── Resilience4JAutoConfiguration.java
                └── advisor/
                    ├── configuration/
                    │   └── RetryOperationAdvisorConfiguration.java
                    └── interceptor/
                        ├── RetryOperationAdvisorInterceptor.java
                        └── exception/
                            └── MethodInvocationException.java
```

---

## Dependências (pom.xml)

```xml
<dependencies>
  <!-- Spring Boot Libraries -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
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
```

---

## Classes

### 1. `RetryOperation` — Anotação customizada

**Pacote:** `com.mds.retry.annotation`

Anotação aplicada em métodos ou classes para ativar a lógica de retry via Resilience4j.

| Atributo | Tipo | Padrão | Descrição |
|---|---|---|---|
| `name` | `String` | `"default"` | Nome da instância de retry no registry |
| `fallbackClass` | `Class<?>` | `RetryOperation.class` | Classe com o método de fallback |
| `fallbackMethod` | `String` | `""` | Nome do método de fallback |

```java
@RetryOperation(
    name = "myRetry",
    fallbackClass = MyFallback.class,
    fallbackMethod = "myFallbackMethod"
)
public void callExternalService() { ... }
```

---

### 2. `Resilience4JProperties` — Propriedades de configuração

**Pacote:** `com.mds.retry.properties`
**Prefix:** `resilience4j.retry`

Mapeia as configurações de retry do `application.yml`.

| Campo | Tipo | Padrão | Descrição |
|---|---|---|---|
| `maxAttempts` | `int` | `3` | Número máximo de tentativas |
| `waitDuration` | `Duration` | `2s` | Tempo de espera entre tentativas |
| `retryExceptions` | `List<String>` | `[]` | Exceções que disparam retry |
| `ignoreExceptions` | `List<String>` | `[]` | Exceções ignoradas pelo retry |

**Exemplo de configuração (`application.yml`):**

```yaml
resilience4j:
  retry:
    instances:
      myRetry:
        maxAttempts: 3
        waitDuration: 2s
        retryExceptions:
          - java.io.IOException
          - org.springframework.web.client.RestClientException
        ignoreExceptions:
          - java.lang.IllegalArgumentException
```

---

### 3. `Resilience4JAutoConfiguration` — Auto-configuração

**Pacote:** `com.mds.retry.config`

Lê as propriedades de `Resilience4JProperties` e instancia o `Map<String, Retry>` com todas as instâncias de retry configuradas. O bean é criado apenas se não houver outro já definido (`@ConditionalOnMissingBean`).

---

### 4. `RetryOperationAdvisorConfiguration` — Configuração do Advisor AOP

**Pacote:** `com.mds.retry.advisor.configuration`

Cria o `Advisor` Spring AOP que intercepta métodos anotados com `@RetryOperation`. Usa `AspectJExpressionPointcut` para identificar os pontos de corte.

---

### 5. `RetryOperationAdvisorInterceptor` — Interceptor AOP

**Pacote:** `com.mds.retry.advisor.interceptor`

Contém a lógica principal de retry:
- Obtém a anotação `@RetryOperation` do método interceptado.
- Decora a chamada com `Retry.decorateCallable` do Resilience4j.
- Em caso de falha após todas as tentativas, resolve o método de fallback (na classe alvo ou em uma classe externa) e o invoca.

---

### 6. `MethodInvocationException` — Exceção de invocação

**Pacote:** `com.mds.retry.advisor.interceptor.exception`

`RuntimeException` que encapsula qualquer `Throwable` lançado durante a execução do método interceptado ou do fallback.

---

## Arquitetura de Fluxo

```
Chamada do método anotado com @RetryOperation
        ↓
RetryOperationAdvisorInterceptor (AOP)
        ↓
Resilience4j Retry.decorateCallable()
        ↓ (em caso de erro)
Novas tentativas conforme configuração
        ↓ (após esgotar tentativas)
Método de Fallback (se configurado)
        ↓ (sem fallback)
MethodInvocationException
```

---

## Como Replicar Este Projeto (Playbook)

### Passo 1 — Criar o módulo Maven

```
GroupId:    com.mds.<domínio>
ArtifactId: spring-<domínio>-pattern
Version:    0.0.1-SNAPSHOT
Packaging:  jar
```

Adicionar ao `pom.xml`: `spring-boot-starter-aop`, `spring-boot-starter-web`, `resilience4j-spring-boot3`, `lombok`.

### Passo 2 — Criar a anotação customizada

Criar em `com.mds.<domínio>.annotation` a anotação com `@Target`, `@Retention(RUNTIME)` e `@Documented`. Definir os atributos: `name`, `fallbackClass`, `fallbackMethod`.

### Passo 3 — Criar as propriedades

Criar em `com.mds.<domínio>.properties` a classe com `@ConfigurationProperties(prefix = "resilience4j.<domínio>")`. Mapear os campos: `instances`, `maxAttempts`, `waitDuration`, `retryExceptions`, `ignoreExceptions`.

### Passo 4 — Criar a auto-configuração

Criar em `com.mds.<domínio>.config` a classe `@Configuration` com `@EnableConfigurationProperties`. Expor um `@Bean` do tipo `Map<String, Retry>` iterando sobre as instâncias configuradas com `RetryConfig.custom()`.

### Passo 5 — Criar o interceptor AOP

Criar em `com.mds.<domínio>.advisor.interceptor` a classe implementando `MethodInterceptor`. No método `invoke`:
1. Ler a anotação `@RetryOperation` do método.
2. Decorar com `Retry.decorateCallable`.
3. Resolver fallback em caso de falha.

### Passo 6 — Criar o advisor de configuração

Criar em `com.mds.<domínio>.advisor.configuration` a classe `@Configurable` com `@ComponentScan("com.mds.<domínio>")`. Expor um `@Bean` do tipo `Advisor` usando `AspectJExpressionPointcut` apontando para a anotação customizada.

### Passo 7 — Criar a exceção de invocação

Criar em `com.mds.<domínio>.advisor.interceptor.exception` a classe `MethodInvocationException extends RuntimeException` com construtor recebendo `Throwable`.

---

## Uso em outro projeto

Adicionar a dependência:

```xml
<dependency>
  <groupId>com.mds.retry</groupId>
  <artifactId>spring-retry-pattern</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Configurar o `application.yml` e anotar os métodos:

```java
@RetryOperation(name = "myRetry")
public String callApi() {
  return restClient.get("/endpoint");
}
```
