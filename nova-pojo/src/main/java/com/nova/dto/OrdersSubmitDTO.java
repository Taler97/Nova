package com.nova.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO implements Serializable {
    @NotNull(message = "地址ID不能为空")
    private Long addressBookId;
    private BigDecimal amount;
    private String remark;
    private LocalDateTime estimatedDeliveryTime;
    private String idempotentToken;
}
