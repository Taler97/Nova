package com.nova.converter;

import com.nova.dto.DishDTO;
import com.nova.entity.Dish;
import com.nova.vo.DishVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface DishConverter {

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    Dish toEntity(DishDTO dto);

    @Mapping(target = "flavors", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    DishVO toVO(Dish dish);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    Dish updateEntity(DishDTO dto);
}
