package com.example.ratelimiter.redis;

import com.example.ratelimiter.core.api.RateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Redis-backed implementation of RateLimitStore using Jedis.
 */
public class RedisRateLimitStore implements RateLimitStore {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitStore.class);

    private final JedisPool jedisPool;
    private final String scriptSha;

    public RedisRateLimitStore(String host, int port) {
        this(host, port, createDefaultPoolConfig());
    }

    public RedisRateLimitStore(String host, int port, JedisPoolConfig poolConfig) {
        this.jedisPool = new JedisPool(poolConfig, host, port);
        this.scriptSha = loadTokenBucketScript();
    }

    private static JedisPoolConfig createDefaultPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);
        config.setMaxIdle(64);
        config.setMinIdle(16);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        return config;
    }

    @Override
    public long increment(String key, Duration ttl) {
        try (var jedis = jedisPool.getResource()) {
            Long result = jedis.incr(key);
            if (result == 1) {
                // First increment, set TTL
                jedis.expire(key, (int) ttl.getSeconds());
            }
            return result != null ? result : 0;
        } catch (JedisException e) {
            log.error("Redis increment failed for key: {}", key, e);
            throw new RuntimeException("Failed to increment counter", e);
        }
    }

    @Override
    public long get(String key) {
        try (var jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            return value != null ? Long.parseLong(value) : 0;
        } catch (JedisException e) {
            log.error("Redis get failed for key: {}", key, e);
            throw new RuntimeException("Failed to get value", e);
        }
    }

    @Override
    public void set(String key, long value, Duration ttl) {
        try (var jedis = jedisPool.getResource()) {
            jedis.setex(key, (int) ttl.getSeconds(), String.valueOf(value));
        } catch (JedisException e) {
            log.error("Redis set failed for key: {}", key, e);
            throw new RuntimeException("Failed to set value", e);
        }
    }

    @Override
    public boolean delete(String key) {
        try (var jedis = jedisPool.getResource()) {
            Long deleted = jedis.del(key);
            return deleted != null && deleted > 0;
        } catch (JedisException e) {
            log.error("Redis delete failed for key: {}", key, e);
            return false;
        }
    }

    @Override
    public Object executeScript(String script, String[] keys, String[] args) {
        try (var jedis = jedisPool.getResource()) {
            return jedis.eval(script, Arrays.asList(keys), Arrays.asList(args));
        } catch (JedisException e) {
            log.error("Redis script execution failed", e);
            throw new RuntimeException("Failed to execute script", e);
        }
    }

    @Override
    public Map<String, Long> multiGet(String... keys) {
        try (var jedis = jedisPool.getResource()) {
            List<String> values = jedis.mget(keys);
            Map<String, Long> result = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                String value = values.get(i);
                result.put(keys[i], value != null ? Long.parseLong(value) : 0L);
            }
            return result;
        } catch (JedisException e) {
            log.error("Redis multiGet failed", e);
            throw new RuntimeException("Failed to get multiple values", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try (var jedis = jedisPool.getResource()) {
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            return false;
        }
    }

    private String loadTokenBucketScript() {
        try (InputStream is = getClass().getResourceAsStream("/lua/token_bucket.lua")) {
            if (is == null) {
                log.warn("Token bucket Lua script not found, using fallback");
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Failed to load token bucket Lua script", e);
            return "";
        }
    }

    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}