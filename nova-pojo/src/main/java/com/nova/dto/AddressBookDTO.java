package com.nova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class AddressBookDTO implements Serializable {
    private Long id;
    @NotBlank(message = "收货人不能为空")
    private String consignee;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "详细地址不能为空")
    private String detail;
    private Integer isDefault;
}
