package com.nova.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nova.context.BaseContext;
import com.nova.dto.OrdersSubmitDTO;
import com.nova.entity.McpOrderRequest;
import com.nova.exception.BaseException;
import com.nova.service.OrderService;
import com.nova.vo.OrdersSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MCP 审批请求执行器：批准后**以申请单的 userId 身份**执行真实业务。
 * <p>
 * 独立 Bean，避免与 {@code McpRequestServiceImpl} 同类自调用导致事务/代理失效。
 * SUBMIT 动作**复用现有幂等令牌机制**（createIdempotentToken + submitOrder 的原子消费），
 * 因此即使执行器重试也不会重复下单。
 */
@Component
@RequiredArgsConstructor
public class McpRequestExecutor {

    private final OrderService orderService;

    /** 执行申请单；成功返回结果描述（如订单号），失败抛异常。 */
    public String execute(McpOrderRequest request) {
        Long previousUserId = BaseContext.getCurrentId();
        try {
            BaseContext.setCurrentId(request.getUserId());
            JSONObject payload = parse(request.getPayloadJson());
            return switch (request.getAction()) {
                case McpOrderRequest.ACTION_SUBMIT -> doSubmit(payload);
                case McpOrderRequest.ACTION_PAY -> doPay(payload);
                case McpOrderRequest.ACTION_CANCEL -> doCancel(payload);
                case McpOrderRequest.ACTION_REMINDER -> doReminder(payload);
                default -> throw new BaseException("不支持的审批动作：" + request.getAction());
            };
        } finally {
            if (previousUserId == null) {
                BaseContext.remove();
            } else {
                BaseContext.setCurrentId(previousUserId);
            }
        }
    }

    private String doSubmit(JSONObject payload) {
        Long addressBookId = payload.getLong("addressBookId");
        if (addressBookId == null) {
            throw new BaseException("申请缺少 addressBookId");
        }
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(addressBookId);
        dto.setRemark(payload.getString("remark"));
        dto.setIdempotentToken(orderService.createIdempotentToken());
        OrdersSubmitVO vo = orderService.submitOrder(dto);
        return "下单成功，订单号 " + vo.getNumber();
    }

    private String doPay(JSONObject payload) {
        String orderNumber = payload.getString("orderNumber");
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new BaseException("申请缺少 orderNumber");
        }
        orderService.paySuccess(orderNumber);
        return "订单 " + orderNumber + " 已支付";
    }

    private String doCancel(JSONObject payload) {
        Long orderId = payload.getLong("orderId");
        if (orderId == null) {
            throw new BaseException("申请缺少 orderId");
        }
        orderService.cancel(orderId);
        return "订单 " + orderId + " 已取消";
    }

    private String doReminder(JSONObject payload) {
        Long orderId = payload.getLong("orderId");
        if (orderId == null) {
            throw new BaseException("申请缺少 orderId");
        }
        orderService.reminder(orderId);
        return "订单 " + orderId + " 已催单";
    }

    private JSONObject parse(String json) {
        if (json == null || json.isBlank()) {
            return new JSONObject();
        }
        return JSONObject.parseObject(json);
    }
}
