package com.example.ratelimiter.core.api;

import java.util.concurrent.TimeUnit;

/**
 * Main interface for rate limiting operations
 * Implementation should be thread-safe
 */
public interface RateLimiter {

    /**
     * Attempts to acquire permission for the given key.
     *
     * @param key - the unique identifier for rate limiting (e.g., user ID, IP address)
     * @return RateLimitResult containing the decision and metadata
     */
    RateLimitResult tryAcquire(String key);


    /**
     * Attempts to acquire permission for the given key with a specific policy.
     *
     * @param key the unique identifier for rate limiting
     * @param policy the rate limit policy to apply
     * @return RateLimitResult containing the decision and metadata
     */
    RateLimitResult tryAcquire(String key, RateLimitPolicy policy);


    /**
     * Resets the rate limit for the given key.
     *
     * @param key the unique identifier to reset
     * @return true if reset was successful
     */
    boolean reset(String key);
}
