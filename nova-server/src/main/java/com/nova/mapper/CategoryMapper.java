package com.nova.mapper;

import com.github.pagehelper.Page;
import com.nova.annotation.AutoFill;
import com.nova.dto.CategoryPageQueryDTO;
import com.nova.entity.Category;
import com.nova.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CategoryMapper {

    @AutoFill(OperationType.INSERT)
    @Insert("INSERT INTO category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "VALUES (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    Page<Category> pageQuery(CategoryPageQueryDTO dto);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category getById(Long id);

    List<Category> list(Integer type);
}
