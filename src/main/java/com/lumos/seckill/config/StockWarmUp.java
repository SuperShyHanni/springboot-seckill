package com.lumos.seckill.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.lumos.seckill.entity.Goods;
import com.lumos.seckill.mapper.GoodsMapper;

@Component
public class StockWarmUp implements CommandLineRunner {
    @Autowired private GoodsMapper goodsMapper;
    @Autowired private StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        List<Goods> goodsList = goodsMapper.selectGoodsList();
        for (Goods goods : goodsList) {
            String key = "seckill:stock:" + goods.getId();
            String value = String.valueOf(goods.getStockCount());
            redisTemplate.opsForValue().set(key, value);
        }
    }
}
