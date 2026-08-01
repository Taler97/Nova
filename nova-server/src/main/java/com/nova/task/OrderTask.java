package com.nova.task;

import com.nova.entity.Orders;
import com.nova.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(fixedDelay = 60000)
    public void autoCancelOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);
        List<Orders> pendingOrders = orderMapper.getByStatusAndTime(Orders.TO_BE_CONFIRMED, deadline);
        for (Orders order : pendingOrders) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("超时未支付，系统自动取消");
            order.setCancelTime(LocalDateTime.now());
            orderMapper.update(order);
            log.info("自动取消订单：{}", order.getNumber());
        }
    }
}
