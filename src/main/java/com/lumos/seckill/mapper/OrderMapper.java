package com.lumos.seckill.mapper;

import com.lumos.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO seckill_order(user_id,goods_id,order_time) VALUES(#{userId},#{goodsId},#{orderTime})")
    int insert(SeckillOrder order);

    @Select("SELECT COUNT(*) FROM seckill_order WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    int count(@Param("userId") Long userId,@Param("goodsId") Long goodsId);
} 