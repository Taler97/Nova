package com.nova.mapper;

import com.nova.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SetmealDishMapper {

    @Insert("<script>" +
            "INSERT INTO setmeal_dish (setmeal_id, dish_id, copies) VALUES " +
            "<foreach collection='list' item='sd' separator=','>" +
            "(#{sd.setmealId}, #{sd.dishId}, #{sd.copies})" +
            "</foreach>" +
            "</script>")
    void insertBatch(List<SetmealDish> setmealDishes);

    @Delete("DELETE FROM setmeal_dish WHERE setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    @Select("SELECT * FROM setmeal_dish WHERE setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    @Select("SELECT COUNT(*) FROM setmeal_dish WHERE dish_id = #{dishId}")
    Integer countByDishId(Long dishId);
}
