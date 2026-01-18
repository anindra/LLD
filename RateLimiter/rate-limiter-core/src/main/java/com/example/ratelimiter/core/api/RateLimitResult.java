package com.example.ratelimiter.core.api;

import java.time.Duration;
import java.util.Optional;

public class RateLimitResult {

    private final boolean allowed;
    private final long remaining;
    private final Duration retryAfter;
    private final long currentCount;
    private final long limit;

    private RateLimitResult(boolean allowed, long remaining, Duration retryAfter,
                            long currentCount, long limit) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfter = retryAfter;
        this.currentCount = currentCount;
        this.limit = limit;
    }

    public static RateLimitResult allowed(long remaining, long currentCount, long limit) {
        return new RateLimitResult(true, remaining, null, currentCount, limit);
    }

    public static RateLimitResult denied(Duration retryAfter, long currentCount, long limit) {
        return new RateLimitResult(false, 0, retryAfter, currentCount, limit);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemaining() {
        return remaining;
    }

    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public long getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", remaining=" + remaining +
                ", retryAfter=" + retryAfter +
                ", currentCount=" + currentCount +
                ", limit=" + limit +
                '}';
    }
}
