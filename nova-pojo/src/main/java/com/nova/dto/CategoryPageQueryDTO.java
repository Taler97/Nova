package com.nova.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryPageQueryDTO implements Serializable {
    private int page;
    private int pageSize;
    private Integer type;
    private String name;
}
