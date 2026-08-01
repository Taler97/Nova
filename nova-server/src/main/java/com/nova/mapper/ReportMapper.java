package com.nova.mapper;

import com.nova.entity.Orders;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    // ============ 旧方法（保留兼容，ReportServiceImpl 导出仍用） ============

    @Select("SELECT SUM(amount) FROM orders WHERE status = #{status} AND order_time BETWEEN #{begin} AND #{end}")
    Double sumByStatusAndTime(Integer status, LocalDateTime begin, LocalDateTime end);

    @Select("SELECT COUNT(*) FROM `user` WHERE create_time BETWEEN #{begin} AND #{end}")
    Integer countNewUsers(LocalDateTime begin, LocalDateTime end);

    @Select("SELECT COUNT(*) FROM `user` WHERE create_time < #{end}")
    Integer countTotalUsers(LocalDateTime end);

    @Select("SELECT COUNT(*) FROM orders WHERE order_time BETWEEN #{begin} AND #{end}")
    Integer countOrders(LocalDateTime begin, LocalDateTime end);

    @Select("SELECT COUNT(*) FROM orders WHERE status = #{status} AND order_time BETWEEN #{begin} AND #{end}")
    Integer countOrdersByStatus(Integer status, LocalDateTime begin, LocalDateTime end);

    @Select("SELECT * FROM orders WHERE order_time BETWEEN #{begin} AND #{end}")
    List<Orders> getOrdersByTime(LocalDateTime begin, LocalDateTime end);

    // ============ 分组查询（替代 N+1 循环） ============

    @Select("SELECT DATE(order_time) AS `day`, COALESCE(SUM(amount), 0) AS `total` " +
            "FROM orders " +
            "WHERE status = #{status} AND order_time BETWEEN #{begin} AND #{end} " +
            "GROUP BY DATE(order_time) ORDER BY `day`")
    List<Map<String, Object>> sumTurnoverGroupByDay(Integer status, LocalDateTime begin, LocalDateTime end);

    @Select("SELECT DATE(create_time) AS `day`, COUNT(*) AS `total` " +
            "FROM `user` " +
            "WHERE create_time BETWEEN #{begin} AND #{end} " +
            "GROUP BY DATE(create_time) ORDER BY `day`")
    List<Map<String, Object>> countNewUsersGroupByDay(LocalDateTime begin, LocalDateTime end);

    @Select("SELECT DATE(order_time) AS `day`, COUNT(*) AS `total` " +
            "FROM orders " +
            "WHERE order_time BETWEEN #{begin} AND #{end} " +
            "GROUP BY DATE(order_time) ORDER BY `day`")
    List<Map<String, Object>> countOrdersGroupByDay(LocalDateTime begin, LocalDateTime end);

    @Select("SELECT DATE(order_time) AS `day`, COUNT(*) AS `total` " +
            "FROM orders " +
            "WHERE status = #{status} AND order_time BETWEEN #{begin} AND #{end} " +
            "GROUP BY DATE(order_time) ORDER BY `day`")
    List<Map<String, Object>> countValidOrdersGroupByDay(Integer status, LocalDateTime begin, LocalDateTime end);
}
