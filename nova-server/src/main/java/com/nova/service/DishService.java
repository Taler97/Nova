package com.nova.service;

import com.nova.dto.DishDTO;
import com.nova.vo.DishVO;
import com.nova.result.PageResult;
import java.util.List;

public interface DishService {
    void save(DishDTO dto);
    PageResult pageQuery(DishDTO dto);
    DishVO getById(Long id);
    void update(DishDTO dto);
    void startOrStop(Integer status, Long id);
    void deleteBatch(List<Long> ids);
    List<DishVO> list(Long categoryId);
}
