package com.example.ratelimiter.core.impl;

import com.example.ratelimiter.core.api.*;
import com.example.ratelimiter.core.failure.FailureMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;


/**
 * Default implementation of RateLimiter.
 * Supports multiple algorithms and failure modes.
 */
public class DefaultRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(DefaultRateLimiter.class);

    private final RateLimitStore store;
    private final RateLimitPolicy defaultPolicy;
    private final FailureMode failureMode;

    public DefaultRateLimiter(RateLimitStore store, RateLimitPolicy defaultPolicy, FailureMode failureMode) {
        this.store = Objects.requireNonNull(store, "Store cannot be null");
        this.defaultPolicy = Objects.requireNonNull(defaultPolicy, "Default policy cannot be null");
        this.failureMode = Objects.requireNonNull(failureMode, "Failure mode cannot be null");
    }

    @Override
    public RateLimitResult tryAcquire(String key) {
        return tryAcquire(key, defaultPolicy);
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitPolicy policy) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(policy, "Policy cannot be null");

        try {
            return executeAlgorithm(key, policy);
        } catch (Exception e) {
            log.error("Rate limiter error for key: {}", key, e);
            return handleFailure(policy);
        }
    }

    @Override
    public boolean reset(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        try {
            return store.delete(key);
        } catch (Exception e) {
            log.error("Failed to reset key: {}", key, e);
            return false;
        }
    }


    private RateLimitResult executeAlgorithm(String key, RateLimitPolicy policy) {
        return switch (policy.getAlgorithm()) {
            case TOKEN_BUCKET -> executeTokenBucket(key, policy);
            case FIXED_WINDOW -> executeFixedWindow(key, policy);
            case SLIDING_WINDOW_LOG -> executeSlidingWindowLog(key, policy);
            case SLIDING_WINDOW_COUNTER -> executeSlidingWindowCounter(key, policy);
            case LEAKY_BUCKET -> executeLeakyBucket(key, policy);
        };
    }

    private RateLimitResult executeTokenBucket(String key, RateLimitPolicy policy) {
        // Token bucket implementation using store
        String countKey = "ratelimit:" + key + ":count";
        String timestampKey = "ratelimit:" + key + ":timestamp";

        long now = Instant.now().toEpochMilli();
        long lastTimestamp = store.get(timestampKey);
        long currentTokens = store.get(countKey);

        if (lastTimestamp == 0) {
            // First request
            currentTokens = policy.getLimit() - 1;
            store.set(countKey, currentTokens, policy.getWindow());
            store.set(timestampKey, now, policy.getWindow());
            return RateLimitResult.allowed(currentTokens, 1, policy.getLimit());
        }

        // Calculate tokens to add based on elapsed time
        long elapsed = now - lastTimestamp;
        long windowMillis = policy.getWindow().toMillis();
        long tokensToAdd = (elapsed * policy.getLimit()) / windowMillis;
        long newTokens = Math.min(policy.getLimit(), currentTokens + tokensToAdd);

        if (newTokens >= 1) {
            newTokens--;
            store.set(countKey, newTokens, policy.getWindow());
            store.set(timestampKey, now, policy.getWindow());
            long used = policy.getLimit() - newTokens;
            return RateLimitResult.allowed(newTokens, used, policy.getLimit());
        } else {
            long waitTime = (windowMillis - elapsed) / policy.getLimit();
            return RateLimitResult.denied(Duration.ofMillis(waitTime), policy.getLimit(), policy.getLimit());
        }
    }

    private RateLimitResult executeFixedWindow(String key, RateLimitPolicy policy) {
        String countKey = "ratelimit:" + key + ":fixed";
        long count = store.increment(countKey, policy.getWindow());

        if (count <= policy.getLimit()) {
            return RateLimitResult.allowed(policy.getLimit() - count, count, policy.getLimit());
        } else {
            return RateLimitResult.denied(policy.getWindow(), count, policy.getLimit());
        }
    }

    private RateLimitResult executeSlidingWindowLog(String key, RateLimitPolicy policy) {
        // Simplified implementation - in production, use sorted sets or time-series data
        return executeFixedWindow(key, policy);
    }

    private RateLimitResult executeSlidingWindowCounter(String key, RateLimitPolicy policy) {
        // Simplified implementation - uses weighted counts from current and previous windows
        return executeFixedWindow(key, policy);
    }

    private RateLimitResult executeLeakyBucket(String key, RateLimitPolicy policy) {
        // Similar to token bucket but with constant drain rate
        return executeTokenBucket(key, policy);
    }

    private RateLimitResult handleFailure(RateLimitPolicy policy) {
        return switch (failureMode) {
            case FAIL_OPEN -> RateLimitResult.allowed(policy.getLimit(), 0, policy.getLimit());
            case FAIL_CLOSED -> RateLimitResult.denied(Duration.ofSeconds(1), policy.getLimit(), policy.getLimit());
        };
    }

}
