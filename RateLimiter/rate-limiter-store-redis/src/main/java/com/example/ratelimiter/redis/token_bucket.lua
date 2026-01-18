-- Token Bucket Rate Limiter Lua Script
-- KEYS[1]: tokens key
-- KEYS[2]: timestamp key
-- ARGV[1]: bucket capacity (max tokens)
-- ARGV[2]: refill rate (tokens per second)
-- ARGV[3]: current timestamp (milliseconds)
-- ARGV[4]: cost (tokens to consume, default 1)

local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local cost = tonumber(ARGV[4]) or 1

-- Get current tokens and last update time
local tokens = tonumber(redis.call('GET', tokens_key))
local last_update = tonumber(redis.call('GET', timestamp_key))

-- Initialize if first request
if tokens == nil then
	tokens = capacity
	last_update = now
end

-- Calculate tokens to add based on elapsed time
local elapsed = now - last_update
local tokens_to_add = (elapsed / 1000) * refill_rate
tokens = math.min(capacity, tokens + tokens_to_add)

-- Try to consume tokens
if tokens >= cost then
	tokens = tokens - cost

	-- Update Redis
	redis.call('SET', tokens_key, tokens)
	redis.call('SET', timestamp_key, now)
	redis.call('EXPIRE', tokens_key, 3600)  -- 1 hour TTL
	redis.call('EXPIRE', timestamp_key, 3600)

	-- Return success with remaining tokens
	return {1, tokens, capacity - tokens}
else
-- Calculate retry after (milliseconds)
	local tokens_needed = cost - tokens
	local retry_after = (tokens_needed / refill_rate) * 1000

	-- Return failure with retry time
	return {0, 0, retry_after}
end