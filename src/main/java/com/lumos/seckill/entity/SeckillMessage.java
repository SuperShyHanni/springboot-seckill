package com.lumos.seckill.entity;

import java.io.Serializable;

public class SeckillMessage implements Serializable {
    private Long userId;
    private Long goodsId;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getGoodsId() {
        return goodsId;
    }
    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
