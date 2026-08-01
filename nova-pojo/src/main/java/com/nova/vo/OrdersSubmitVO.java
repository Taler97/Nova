package com.nova.vo;

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
public class OrdersSubmitVO implements Serializable {
    private Long id;
    private String number;
    private BigDecimal orderAmount;
    private LocalDateTime orderTime;
}
