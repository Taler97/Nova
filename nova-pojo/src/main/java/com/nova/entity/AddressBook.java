package com.nova.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBook implements Serializable {
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String detail;
    private Integer isDefault;
    private LocalDateTime createTime;
}
