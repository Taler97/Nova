package com.nova.service;

import com.nova.dto.OrdersConfirmDTO;
import com.nova.dto.OrdersPageQueryDTO;
import com.nova.dto.OrdersRejectionDTO;
import com.nova.dto.OrdersSubmitDTO;
import com.nova.result.PageResult;
import com.nova.vo.OrderVO;
import com.nova.vo.OrdersSubmitVO;

public interface OrderService {
    OrdersSubmitVO submitOrder(OrdersSubmitDTO dto);
    String createIdempotentToken();
    void paySuccess(String orderNumber);
    void cancel(Long id);
    PageResult historyOrders(OrdersPageQueryDTO dto);
    OrderVO userDetail(Long id);
    OrderVO adminDetail(Long id);
    PageResult adminSearch(Integer page, Integer pageSize, String number, String phone, Integer status);
    void confirm(OrdersConfirmDTO dto);
    void rejection(OrdersRejectionDTO dto);
    void delivery(Long id);
    void complete(Long id);
    void reminder(Long id);
}
