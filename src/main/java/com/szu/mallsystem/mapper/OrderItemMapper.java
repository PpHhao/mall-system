package com.szu.mallsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szu.mallsystem.entity.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("""
            SELECT COUNT(1)
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            WHERE o.user_id = #{userId}
              AND o.deleted = 0
              AND o.status = #{orderStatus}
              AND o.id = #{orderId}
              AND oi.product_id = #{productId}
            """)
    long countPurchasedProductInOrder(@Param("userId") Long userId,
                                     @Param("orderId") Long orderId,
                                     @Param("productId") Long productId,
                                     @Param("orderStatus") Integer orderStatus);
}
