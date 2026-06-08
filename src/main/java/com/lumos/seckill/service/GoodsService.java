package com.lumos.seckill.service;

import com.lumos.seckill.entity.Goods;
import com.lumos.seckill.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GoodsService {
    @Autowired private GoodsMapper goodsMapper;
    public List<Goods> getGoodsList(){
        return goodsMapper.selectGoodsList();
    }
}
