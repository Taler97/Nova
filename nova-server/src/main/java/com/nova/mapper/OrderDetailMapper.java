package com.nova.mapper;

import com.nova.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    @Select("<script>" +
            "SELECT * FROM order_detail WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<OrderDetail> getByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Insert("<script>" +
            "INSERT INTO order_detail (order_id, dish_id, number, amount, name, image, dish_flavor) VALUES " +
            "<foreach collection='list' item='d' separator=','>" +
            "(#{d.orderId}, #{d.dishId}, #{d.number}, #{d.amount}, #{d.name}, #{d.image}, #{d.dishFlavor})" +
            "</foreach>" +
            "</script>")
    void insertBatch(List<OrderDetail> details);
}
