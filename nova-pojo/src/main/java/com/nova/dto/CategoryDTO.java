package com.nova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {
    private Long id;
    @NotNull(message = "分类类型不能为空")
    private Integer type;
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private Integer sort;
}
