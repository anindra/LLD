package com.example.ratelimiter.core.api;

/**
 * Strategy for resolving rate limit keys from context.
 * Allows flexible key generation strategies (e.g., by user, IP, API key, etc.)
 */
@FunctionalInterface
public interface RateLimitKeyResolver {

    /**
     * Resolves a rate limit key from the given context.
     *
     * @param context the context object (e.g., request, user, etc.)
     * @return the resolved key for rate limiting
     */
    String resolve(Object context);

    /**
     * Default resolver that uses toString() on the context.
     */
    static RateLimitKeyResolver defaultResolver() {
        return context -> context == null ? "anonymous" : context.toString();
    }

    /**
     * Resolver that extracts a specific property from context.
     */
    static RateLimitKeyResolver propertyResolver(String property) {
        return context -> {
            if (context == null) {
                return "anonymous";
            }
            // Simple implementation - can be enhanced with reflection
            return context.toString() + ":" + property;
        };
    }

    /**
     * Combines multiple resolvers with a separator.
     */
    static RateLimitKeyResolver composite(String separator, RateLimitKeyResolver... resolvers) {
        return context -> {
            StringBuilder key = new StringBuilder();
            for (int i = 0; i < resolvers.length; i++) {
                if (i > 0) {
                    key.append(separator);
                }
                key.append(resolvers[i].resolve(context));
            }
            return key.toString();
        };
    }
}
