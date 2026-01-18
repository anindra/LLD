package com.example.ratelimiter.core.failure;

/**
 * Defines behavior when rate limiter encounters errors.
 */
public enum FailureMode {

    /**
     * Allow requests through when errors occur.
     * Prioritizes availability over rate limiting guarantees.
     */
    FAIL_OPEN,

    /**
     * Deny requests when errors occur.
     * Prioritizes rate limiting guarantees over availability.
     */
    FAIL_CLOSED
}
