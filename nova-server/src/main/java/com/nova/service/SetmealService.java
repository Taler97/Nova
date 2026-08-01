package com.nova.service;

import com.nova.dto.SetmealDTO;
import com.nova.result.PageResult;
import com.nova.vo.SetmealVO;
import java.util.List;

public interface SetmealService {
    void save(SetmealDTO dto);
    SetmealVO getById(Long id);
    void update(SetmealDTO dto);
    PageResult pageQuery(SetmealDTO dto);
    void startOrStop(Integer status, Long id);
    void deleteBatch(List<Long> ids);
    List<SetmealVO> listByCategoryId(Long categoryId);
}
