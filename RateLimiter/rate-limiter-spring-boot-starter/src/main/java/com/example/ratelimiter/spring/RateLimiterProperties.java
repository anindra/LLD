package com.example.ratelimiter.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Rate Limiter.
 */
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    /**
     * Enable or disable rate limiting.
     */
    private boolean enabled = true;

    /**
     * Default rate limit (requests per window).
     */
    private int defaultLimit = 100;

    /**
     * Default time window (ISO-8601 duration format, e.g., PT1M for 1 minute).
     */
    private String defaultWindow = "PT1M";

    /**
     * Default rate limiting algorithm.
     */
    private String defaultAlgorithm = "TOKEN_BUCKET";

    /**
     * Failure mode when errors occur.
     */
    private String failureMode = "FAIL_OPEN";

    /**
     * Redis configuration.
     */
    private RedisProperties redis = new RedisProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public String getDefaultWindow() {
        return defaultWindow;
    }

    public void setDefaultWindow(String defaultWindow) {
        this.defaultWindow = defaultWindow;
    }

    public String getDefaultAlgorithm() {
        return defaultAlgorithm;
    }

    public void setDefaultAlgorithm(String defaultAlgorithm) {
        this.defaultAlgorithm = defaultAlgorithm;
    }

    public String getFailureMode() {
        return failureMode;
    }

    public void setFailureMode(String failureMode) {
        this.failureMode = failureMode;
    }

    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    public static class RedisProperties {

        /**
         * Enable Redis store.
         */
        private boolean enabled = true;

        /**
         * Redis host.
         */
        private String host = "localhost";

        /**
         * Redis port.
         */
        private int port = 6379;

        /**
         * Redis password (optional).
         */
        private String password;

        /**
         * Redis database index.
         */
        private int database = 0;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }
    }
}
