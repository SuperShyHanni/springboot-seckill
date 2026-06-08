package com.lumos.seckill.entity;

import java.math.BigDecimal;

public class Goods {
    private Long id;
    private String goodsName;
    private BigDecimal goodsPrice;
    private Integer stockCount;

    public Long getId() {        
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getGoodsName() {        
        return this.goodsName;
    }
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public BigDecimal getGoodsPrice() {        
        return this.goodsPrice;
    }
    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public Integer getStockCount() {        
        return this.stockCount;
    }
    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }
}
