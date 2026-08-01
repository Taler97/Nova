package com.nova.mapper;

import com.nova.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DishFlavorMapper {

    @Select("SELECT * FROM dish_flavor WHERE dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    @Insert("<script>" +
            "INSERT INTO dish_flavor (dish_id, name, value) VALUES " +
            "<foreach collection='list' item='f' separator=','>" +
            "(#{f.dishId}, #{f.name}, #{f.value})" +
            "</foreach>" +
            "</script>")
    void insertBatch(List<DishFlavor> flavors);

    @Delete("DELETE FROM dish_flavor WHERE dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
}
