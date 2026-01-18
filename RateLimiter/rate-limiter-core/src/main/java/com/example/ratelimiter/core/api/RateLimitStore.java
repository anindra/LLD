package com.example.ratelimiter.core.api;

import java.time.Duration;
import java.util.Map;

/**
 * Storage backend for rate limit state.
 * Implementations must be thread-safe.
 */

public interface RateLimitStore {

    /**
     * Increments the counter for the given key.
     *
     * @param key the rate limit key
     * @param ttl time-to-live for the key
     * @return the new count after increment
     */
    long increment(String key, Duration ttl);

    /**
     * Gets the current count for the given key.
     *
     * @param key the rate limit key
     * @return the current count, or 0 if key doesn't exist
     */
    long get(String key);

    /**
     * Sets a value for the given key with TTL.
     *
     * @param key the rate limit key
     * @param value the value to set
     * @param ttl time-to-live for the key
     */
    void set(String key, long value, Duration ttl);

    /**
     * Deletes the given key.
     *
     * @param key the rate limit key
     * @return true if key was deleted
     */
    boolean delete(String key);

    /**
     * Executes a Lua script atomically (for algorithms requiring atomic operations).
     *
     * @param script the Lua script to execute
     * @param keys list of keys to pass to the script
     * @param args list of arguments to pass to the script
     * @return the result of script execution
     */
    Object executeScript(String script, String[] keys, String[] args);

    /**
     * Gets multiple values atomically.
     *
     * @param keys the keys to retrieve
     * @return map of key-value pairs
     */
    Map<String, Long> multiGet(String... keys);

    /**
     * Checks if the store is available and healthy.
     *
     * @return true if store is healthy
     */
    boolean isHealthy();

}
