package com.example.ratelimiter.core.api;

public enum RateLimitAlgorithm {

    /**
     * Token bucket algorithm: tokens are added at a fixed rate,
     * requests consume tokens. Allows bursts up to bucket capacity.
     */
    TOKEN_BUCKET,

    /**
     * Fixed window algorithm: counts requests in fixed time windows.
     * Simple but can allow bursts at window boundaries.
     */
    FIXED_WINDOW,

    /**
     * Sliding window log: maintains a log of request timestamps.
     * Most accurate but higher memory usage.
     */
    SLIDING_WINDOW_LOG,

    /**
     * Sliding window counter: approximates sliding window using
     * two fixed windows. Good balance of accuracy and efficiency.
     */
    SLIDING_WINDOW_COUNTER,

    /**
     * Leaky bucket: requests are processed at a constant rate.
     * Smooths out bursts.
     */
    LEAKY_BUCKET
}
