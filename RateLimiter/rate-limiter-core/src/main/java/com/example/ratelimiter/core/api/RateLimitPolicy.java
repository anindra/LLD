package com.example.ratelimiter.core.api;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for rate limiting policy
 */
public class RateLimitPolicy {

    private final int limit;
    private final Duration window;
    private final RateLimitAlgorithm algorithm;

    public RateLimitPolicy(int limit, Duration window, RateLimitAlgorithm algorithm) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }

        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window or window must be greater than 0");
        }

        this.limit = limit;
        this.window = window;
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getLimit() {
        return limit;
    }

    public Duration getWindow() {
        return window;
    }

    public RateLimitAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimitPolicy that = (RateLimitPolicy) o;
        return limit == that.limit &&
                Objects.equals(window, that.window) &&
                algorithm == that.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, window, algorithm);
    }

    public static class Builder {
        private int limit;
        private Duration window;
        private RateLimitAlgorithm algorithm = RateLimitAlgorithm.TOKEN_BUCKET;

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder window(Duration window) {
            this.window = window;
            return this;
        }

        public Builder algorithm(RateLimitAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public RateLimitPolicy build() {
            return new RateLimitPolicy(limit, window, algorithm);
        }
    }
}
