package com.szu.mallsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szu.mallsystem.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count, COALESCE(SUM(amount), 0) as total " +
            "FROM payments WHERE created_at >= #{startDate} AND created_at <= #{endDate} " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> getPaymentStatistics(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Select("SELECT pay_method, COUNT(*) as count, COALESCE(SUM(amount), 0) as total " +
            "FROM payments WHERE created_at >= #{startDate} AND created_at <= #{endDate} " +
            "GROUP BY pay_method")
    List<Map<String, Object>> getPaymentMethodStatistics(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    @Select("SELECT status, COUNT(*) as count, COALESCE(SUM(amount), 0) as total " +
            "FROM payments WHERE created_at >= #{startDate} AND created_at <= #{endDate} " +
            "GROUP BY status")
    List<Map<String, Object>> getPaymentStatusStatistics(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}
