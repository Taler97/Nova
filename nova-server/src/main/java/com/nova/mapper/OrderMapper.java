package com.nova.mapper;

import com.github.pagehelper.Page;
import com.nova.dto.OrdersPageQueryDTO;
import com.nova.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    Page<Orders> pageQuery(OrdersPageQueryDTO dto);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Orders getById(Long id);

    @Select("SELECT * FROM orders WHERE number = #{number}")
    Orders getByNumber(String number);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY order_time DESC")
    List<Orders> listByUserId(Long userId);

    void insert(Orders order);

    void update(Orders order);

    Page<Orders> listByUserIdAndStatus(Long userId, Integer status);

    Page<Orders> adminSearch(String number, String phone, Integer status);

    @Select("SELECT * FROM orders WHERE status = #{status} AND order_time < #{deadline}")
    List<Orders> getByStatusAndTime(Integer status, LocalDateTime deadline);
}
