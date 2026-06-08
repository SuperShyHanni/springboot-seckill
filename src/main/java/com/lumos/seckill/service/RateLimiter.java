package com.lumos.seckill.service;

import java.util.Collections;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.script = new DefaultRedisScript<>();
        this.script.setScriptSource(
            new ResourceScriptSource(new ClassPathResource("lua/ratelimit.lua"))
        );
        this.script.setResultType(Long.class);
    }

    public boolean tryAcquire(String key,int rate,int capacity) {
        long now = System.currentTimeMillis() / 1000;   // 当前时间（秒）

        Long result = redisTemplate.execute(
            script,                              // 要执行的脚本
            Collections.singletonList(key),      // KEYS：只有一个 key
            String.valueOf(rate),                // ARGV[1] rate（每秒1000个）
            String.valueOf(capacity),            // ARGV[2] capacity（桶容量1000）
            String.valueOf(now),                 // ARGV[3] now
            "1"                                  // ARGV[4] requested
        );

        return result != null && result == 1;   // 1=放行 → true
        }
}
