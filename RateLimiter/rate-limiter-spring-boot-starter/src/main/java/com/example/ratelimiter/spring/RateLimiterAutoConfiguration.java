package com.example.ratelimiter.spring;

import com.example.ratelimiter.core.api.RateLimitAlgorithm;
import com.example.ratelimiter.core.api.RateLimitPolicy;
import com.example.ratelimiter.core.api.RateLimitStore;
import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.failure.FailureMode;
import com.example.ratelimiter.core.impl.DefaultRateLimiter;
import com.example.ratelimiter.redis.RedisRateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * Spring Boot auto-configuration for Rate Limiter.
 */
@AutoConfiguration
@ConditionalOnClass(RateLimiter.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@ConditionalOnProperty(prefix = "rate-limiter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimiterAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rate-limiter.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitStore rateLimitStore(RateLimiterProperties properties) {
        log.info("Configuring Redis rate limit store with host: {}, port: {}",
                properties.getRedis().getHost(), properties.getRedis().getPort());

        return new RedisRateLimitStore(
                properties.getRedis().getHost(),
                properties.getRedis().getPort()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitPolicy defaultRateLimitPolicy(RateLimiterProperties properties) {
        log.info("Configuring default rate limit policy: limit={}, window={}, algorithm={}",
                properties.getDefaultLimit(),
                properties.getDefaultWindow(),
                properties.getDefaultAlgorithm());

        return RateLimitPolicy.builder()
                .limit(properties.getDefaultLimit())
                .window(Duration.parse(properties.getDefaultWindow()))
                .algorithm(RateLimitAlgorithm.valueOf(properties.getDefaultAlgorithm().toUpperCase()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiter rateLimiter(RateLimitStore store,
                                   RateLimitPolicy defaultPolicy,
                                   RateLimiterProperties properties) {
        log.info("Configuring rate limiter with failure mode: {}", properties.getFailureMode());

        FailureMode failureMode = FailureMode.valueOf(properties.getFailureMode().toUpperCase());
        return new DefaultRateLimiter(store, defaultPolicy, failureMode);
    }
}
