package com.nova.dto;

import com.nova.entity.SetmealDish;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealDTO implements Serializable {
    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer status;
    private String description;
    private String image;
    private int page;
    private int pageSize;
    private List<SetmealDish> setmealDishes;
}
