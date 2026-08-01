package com.nova.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrdersRejectionDTO implements Serializable {
    private Long id;
    private String rejectionReason;
}
