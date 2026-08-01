package com.nova.mapper;

import com.github.pagehelper.Page;
import com.nova.annotation.AutoFill;
import com.nova.dto.SetmealDTO;
import com.nova.entity.Setmeal;
import com.nova.enumeration.OperationType;
import com.nova.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SetmealMapper {

    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    @Select("SELECT * FROM setmeal WHERE id = #{id}")
    Setmeal getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealDTO dto);

    @Delete("DELETE FROM setmeal WHERE id = #{id}")
    void deleteById(Long id);

    void deleteBatch(List<Long> ids);

    @Select("SELECT COUNT(*) FROM setmeal WHERE category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Select("SELECT COUNT(*) FROM setmeal WHERE status = #{status}")
    Integer countByStatus(Integer status);

    @Select("SELECT * FROM setmeal WHERE category_id = #{categoryId} AND status = 1 ORDER BY create_time DESC")
    List<Setmeal> listByCategoryId(Long categoryId);

    @Select("SELECT id FROM setmeal")
    List<Long> getAllIds();
}
