package com.nova.entity;

import com.nova.enumeration.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {
    /** 订单状态常量 — 由 OrderStatus 枚举定义 */
    public static final Integer PENDING_PAYMENT      = OrderStatus.PENDING_PAYMENT.getCode();
    public static final Integer TO_BE_CONFIRMED       = OrderStatus.TO_BE_CONFIRMED.getCode();
    public static final Integer CONFIRMED             = OrderStatus.CONFIRMED.getCode();
    public static final Integer DELIVERY_IN_PROGRESS  = OrderStatus.DELIVERY_IN_PROGRESS.getCode();
    public static final Integer COMPLETED             = OrderStatus.COMPLETED.getCode();
    public static final Integer CANCELLED             = OrderStatus.CANCELLED.getCode();

    private Long id;
    private String number;
    private Integer status;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime orderTime;
    private Integer payMethod;
    private String consignee;
    private String phone;
    private String address;
    private String remark;
    private String cancelReason;
    private String rejectionReason;
    private LocalDateTime cancelTime;
    private LocalDateTime estimatedDeliveryTime;
}
