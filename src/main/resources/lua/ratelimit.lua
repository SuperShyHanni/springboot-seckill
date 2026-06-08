-- 令牌桶限流脚本（整段被 Redis 原子执行，中间不会被别的请求插队）

-- 1. 取参数（ARGV 传进来都是字符串，要 tonumber 转数字）
local rate = tonumber(ARGV[1])        -- 每秒放多少令牌
local capacity = tonumber(ARGV[2])    -- 桶容量（令牌上限）
local now = tonumber(ARGV[3])         -- 当前时间（秒）
local requested = tonumber(ARGV[4])   -- 本次要几个令牌（一般 1）

-- 2. 读出桶的当前状态
local data = redis.call('hmget', KEYS[1], 'tokens', 'timestamp')
local tokens = data[1]
local ts = data[2]

-- 3. 第一次访问（字段不存在取到 false）→ 初始化成满桶
if tokens == false then
    tokens = capacity
    ts = now
else
    tokens = tonumber(tokens)
    ts = tonumber(ts)
end

-- 4. 懒补令牌：按流逝时间补，封顶到容量
local elapsed = math.max(0, now - ts)
tokens = math.min(capacity, tokens + elapsed * rate)

-- 5. 判断够不够（先记结果，不急着 return）
local allowed = 0
if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
end

-- 6. 写回新状态（放行与否都要写：令牌数和时间都变了）
redis.call('hmset', KEYS[1], 'tokens', tokens, 'timestamp', now)
redis.call('expire', KEYS[1], 60)   -- 60秒没人用就自动清掉，省内存

-- 7. 最后才返回结果
return allowed
