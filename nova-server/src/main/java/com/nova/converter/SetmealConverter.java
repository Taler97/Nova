package com.nova.converter;

import com.nova.dto.SetmealDTO;
import com.nova.entity.Setmeal;
import com.nova.vo.SetmealVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface SetmealConverter {

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    Setmeal toEntity(SetmealDTO dto);

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "setmealDishes", ignore = true)
    SetmealVO toVO(Setmeal setmeal);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    Setmeal updateEntity(SetmealDTO dto);
}
