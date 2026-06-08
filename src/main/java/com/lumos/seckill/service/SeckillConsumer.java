package com.lumos.seckill.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lumos.seckill.config.RabbitConfig;
import com.lumos.seckill.entity.SeckillMessage;
import com.lumos.seckill.entity.SeckillOrder;
import com.lumos.seckill.mapper.GoodsMapper;
import com.lumos.seckill.mapper.OrderMapper;

import java.util.Date;

@Component
public class SeckillConsumer {
    @Autowired private GoodsMapper goodsMapper;
    @Autowired private OrderMapper orderMapper;

    @RabbitListener(queues = RabbitConfig.SECKILL_QUEUE)
    @Transactional
    public void handle(SeckillMessage message) {
        Long userId = message.getUserId();
        Long goodsId = message.getGoodsId();
        if (orderMapper.count(userId, goodsId) > 0) {
            return;
        }
        int n = goodsMapper.reduceStock(goodsId);
        if (n == 0) return;
        SeckillOrder order = new SeckillOrder();
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setOrderTime(new Date());
        orderMapper.insert(order);
    }
}
