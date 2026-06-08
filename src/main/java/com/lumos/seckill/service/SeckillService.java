package com.lumos.seckill.service;

import com.lumos.seckill.config.RabbitConfig;
import com.lumos.seckill.entity.Goods;
import com.lumos.seckill.entity.SeckillMessage;
import com.lumos.seckill.entity.SeckillOrder;
import com.lumos.seckill.mapper.GoodsMapper;
import com.lumos.seckill.mapper.OrderMapper;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SeckillService {
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public String seckill(Long userId,Long goodsId){
        Long remain = redisTemplate.opsForValue().decrement("seckill:stock:" + goodsId);
        if (remain == null || remain < 0) {
            return "已抢完";
        }

        int ordercount = orderMapper.count(userId, goodsId);
        if (ordercount > 0) {
            return "请勿重复抢购";
        }

        Goods goods = goodsMapper.selectGoodsById(goodsId);
        if (goods.getStockCount() <= 0) {return "已抢完";}
        int n = goodsMapper.reduceStock(goodsId);
        if (n == 0) {
            return "已抢完";
        }

        SeckillOrder order = new SeckillOrder();
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setOrderTime(new Date());
        orderMapper.insert(order);
        return "抢购成功";
    }

    public String seckillAsync(Long userId,Long goodsId){
        Long remain = redisTemplate.opsForValue().decrement("seckill:stock:" + goodsId);
        if (remain == null || remain < 0) {
            return "已抢完";
        }

        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setUserId(userId);
        seckillMessage.setGoodsId(goodsId);

        rabbitTemplate.convertAndSend(RabbitConfig.SECKILL_QUEUE,seckillMessage);
        return "排队中，请稍等";
    }
}