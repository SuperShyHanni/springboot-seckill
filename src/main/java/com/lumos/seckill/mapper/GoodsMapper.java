package com.lumos.seckill.mapper;

import com.lumos.seckill.entity.Goods;        
import org.apache.ibatis.annotations.Mapper;   
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;  

@Mapper
public interface GoodsMapper {
    @Select("SELECT * FROM goods")
    List<Goods> selectGoodsList();

    @Update("UPDATE goods SET stock_count = stock_count - 1 WHERE id = #{goodsId} AND stock_count > 0")
    int reduceStock(Long goodsId);

    @Select("SELECT * FROM goods WHERE id = #{id}")
    Goods selectGoodsById(Long id);
}
