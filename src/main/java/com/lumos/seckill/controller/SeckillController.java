package com.lumos.seckill.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumos.seckill.service.SeckillService;
import com.lumos.seckill.service.RateLimiter;
import com.lumos.seckill.util.UserContext;

@RestController
public class SeckillController {
    private final SeckillService seckillService;
    private final RateLimiter rateLimiter;
    public SeckillController(SeckillService seckillService,RateLimiter rateLimiter) {
        this.seckillService = seckillService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/seckill/do")
    public String doSeckill(@RequestParam("goodsId") Long goodsId) {
        if (!rateLimiter.tryAcquire("ratelimit:seckill",1000,1000)) {
            return "系统繁忙，请稍后再试";
        }

        Long userId = UserContext.get();

        if (!rateLimiter.tryAcquire("ratelimit:user:" + userId, 5, 5)) {
            return "操作过于频繁，请稍后再试";
        }
        
        return seckillService.seckill(userId,goodsId);
    }

    @PostMapping("/seckill/async")
    public String doSeckillAsync(@RequestParam("goodsId") Long goodsId){
        if (!rateLimiter.tryAcquire("ratelimit:seckill",1000,1000)) {
            return "系统繁忙，请稍后再试";
        }

        Long userId = UserContext.get();

        if (!rateLimiter.tryAcquire("ratelimit:user:" + userId, 5, 5)) {
            return "操作过于频繁，请稍后再试";
        }

        return seckillService.seckillAsync(userId,goodsId);
    }
}
