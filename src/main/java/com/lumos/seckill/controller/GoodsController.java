package com.lumos.seckill.controller;

import com.lumos.seckill.entity.Goods;
import com.lumos.seckill.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class GoodsController {
    @Autowired private GoodsService goodsService;
    @GetMapping("/goods/list")
    public List<Goods> list(){
        return goodsService.getGoodsList();
    }
}
