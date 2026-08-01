package com.nova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {
    private Long id;
    @NotBlank(message = "员工姓名不能为空")
    private String name;
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private String sex;
    private String idNumber;
}
