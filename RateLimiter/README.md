# Distributed Rate Limiter
A flexible, distributed rate limiting library for Java applications with support for multiple algorithms and storage backends.

## Architecture

```
distributed-rate-limiter/
    ├── rate-limiter-core/                 ← Pure Java core library
    ├── rate-limiter-store-redis/          ← Redis storage implementation
    ├── rate-limiter-spring-boot-starter/  ← Spring Boot auto-configuration
    └── rate-limiter-gateway/              ← Spring Cloud Gateway integration
```

## Features

Multiple Algorithms: Token Bucket, Fixed Window, Sliding Window (Log & Counter), Leaky Bucket
Distributed: Redis-backed storage for multi-instance deployments
Flexible: Pure Java core with optional Spring integration
Resilient: Configurable failure modes (fail-open or fail-closed)
Observable: Built-in metrics and rate limit headers


## Quick Start
### Maven Dependencies
NOTE: Need to local build the artifact to be able to use it
```xml
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>rate-limiter-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

### Configuration
```yaml
    rate-limiter:
        enabled: true
        default-limit: 100
        default-window: PT1M  # 1 minute
        default-algorithm: TOKEN_BUCKET
        failure-mode: FAIL_OPEN
        redis:
            host: localhost
            port: 6379
```

### Usage
#### Programmatic Usage
```java
    @Autowired
    private RateLimiter rateLimiter;

    public void handleRequest(String userId) {
        RateLimitResult result = rateLimiter.tryAcquire(userId);

        if (result.isAllowed()) {
            // Process request
            System.out.println("Remaining: " + result.getRemaining());
        } else {
            // Rate limit exceeded
            result.getRetryAfter().ifPresent(duration ->
                System.out.println("Retry after: " + duration.getSeconds() + "s")
            );
        }
    }
```

### Spring Cloud Gateway
```yaml
    spring:
        cloud:
            gateway:
                routes:
                    - id: api-route
                      uri: http://api.example.com
                      predicates:
                        - Path=/api/**
                      filters:
                        - name: RateLimitGatewayFilter
                          args:
                            keyType: IP
```

### Custom Policies
```java
    RateLimitPolicy customPolicy = RateLimitPolicy.builder()
        .limit(50)
        .window(Duration.ofSeconds(30))
        .algorithm(RateLimitAlgorithm.SLIDING_WINDOW_COUNTER)
        .build();

    RateLimitResult result = rateLimiter.tryAcquire(key, customPolicy);
```

## Algorithms

### Token Bucket
- Allows bursts up to bucket capacity
- Tokens refill at a constant rate
- Best for: APIs with burst traffic patterns

### Fixed Window
- Counts requests in fixed time windows
- Simple and memory-efficient
- Caveat: Can allow bursts at window boundaries

### Sliding Window Log
- Most accurate sliding window implementation
- Maintains timestamp log
- Best for: Strict rate limiting requirements

### Sliding Window Counter
- Approximates sliding window using two counters
- Good balance of accuracy and efficiency
- Best for: Most production use cases

### Leaky Bucket
- Processes requests at constant rate
- Smooths out bursts
- Best for: Systems requiring steady request flow

## Building from Source
```bash
    # Build all modules
    mvn clean install

    # Build specific module
    cd rate-limiter-core
    mvn clean install
```



### License
This project is licensed under the MIT License.

### Contributing
Contributions are welcome! Please submit pull requests or open issues for bugs and feature requests.