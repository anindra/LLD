package com.example.ratelimiter.gateway;

import com.example.ratelimiter.core.api.RateLimitPolicy;
import com.example.ratelimiter.core.api.RateLimitResult;
import com.example.ratelimiter.core.api.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Spring Cloud Gateway filter for rate limiting.
 */
@Component
public class RateLimitGatewayFilter extends AbstractGatewayFilterFactory<RateLimitGatewayFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(RateLimitGatewayFilter.class);

    private static final String X_RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";

    private final RateLimiter rateLimiter;

    public RateLimitGatewayFilter(RateLimiter rateLimiter) {
        super(Config.class);
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "RateLimiter cannot be null");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String key = resolveKey(request, config);

            log.debug("Rate limiting request for key: {}", key);

            RateLimitResult result = config.getPolicy() != null
                    ? rateLimiter.tryAcquire(key, config.getPolicy())
                    : rateLimiter.tryAcquire(key);

            ServerHttpResponse response = exchange.getResponse();
            addRateLimitHeaders(response, result);

            if (result.isAllowed()) {
                log.debug("Request allowed for key: {}", key);
                return chain.filter(exchange);
            } else {
                log.warn("Request denied for key: {} - limit exceeded", key);
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                result.getRetryAfter().ifPresent(retryAfter ->
                        response.getHeaders().add("Retry-After", String.valueOf(retryAfter.getSeconds()))
                );

                return response.setComplete();
            }
        };
    }

    private String resolveKey(ServerHttpRequest request, Config config) {
        return switch (config.getKeyType()) {
            case IP -> getClientIp(request);
            case USER -> getUserId(request);
            case API_KEY -> getApiKey(request);
            case PATH -> request.getPath().value();
            default -> "global";
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    private String getUserId(ServerHttpRequest request) {
        // Extract from Authorization header or custom header
        String userId = request.getHeaders().getFirst("X-User-Id");
        return userId != null ? userId : "anonymous";
    }

    private String getApiKey(ServerHttpRequest request) {
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        return apiKey != null ? apiKey : "no-key";
    }

    private void addRateLimitHeaders(ServerHttpResponse response, RateLimitResult result) {
        response.getHeaders().add(X_RATE_LIMIT_LIMIT, String.valueOf(result.getLimit()));
        response.getHeaders().add(X_RATE_LIMIT_REMAINING, String.valueOf(result.getRemaining()));

        result.getRetryAfter().ifPresent(retryAfter ->
                response.getHeaders().add(X_RATE_LIMIT_RESET, String.valueOf(System.currentTimeMillis() + retryAfter.toMillis()))
        );
    }

    public static class Config {

        private KeyType keyType = KeyType.IP;
        private RateLimitPolicy policy;

        public KeyType getKeyType() {
            return keyType;
        }

        public void setKeyType(KeyType keyType) {
            this.keyType = keyType;
        }

        public RateLimitPolicy getPolicy() {
            return policy;
        }

        public void setPolicy(RateLimitPolicy policy) {
            this.policy = policy;
        }
    }

    public enum KeyType {
        IP,
        USER,
        API_KEY,
        PATH,
        GLOBAL
    }
}
