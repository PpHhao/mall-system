package com.szu.mallsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szu.mallsystem.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
