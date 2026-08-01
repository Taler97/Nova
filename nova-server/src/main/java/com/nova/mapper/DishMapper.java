package com.nova.mapper;

import com.github.pagehelper.Page;
import com.nova.annotation.AutoFill;
import com.nova.dto.DishDTO;
import com.nova.entity.Dish;
import com.nova.enumeration.OperationType;
import com.nova.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DishMapper {

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishDTO dto);

    @Select("SELECT * FROM dish WHERE id = #{id}")
    Dish getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Delete("DELETE FROM dish WHERE id = #{id}")
    void deleteById(Long id);

    void deleteBatch(List<Long> ids);

    List<Dish> list(Long categoryId);

    @Select("SELECT COUNT(*) FROM dish WHERE category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Select("SELECT COUNT(*) FROM dish WHERE status = #{status}")
    Integer countByStatus(Integer status);
}
