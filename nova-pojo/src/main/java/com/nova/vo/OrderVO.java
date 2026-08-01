package com.nova.vo;

import com.nova.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {
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
    private LocalDateTime estimatedDeliveryTime;
    private List<OrderDetail> orderDetails;
}
