package com.nova.service;

import com.nova.dto.CategoryDTO;
import com.nova.dto.CategoryPageQueryDTO;
import com.nova.entity.Category;
import com.nova.result.PageResult;
import java.util.List;

public interface CategoryService {
    PageResult pageQuery(CategoryPageQueryDTO dto);
    void save(CategoryDTO dto);
    void update(CategoryDTO dto);
    void deleteById(Long id);
    List<Category> list(Integer type);
}
