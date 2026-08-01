package com.nova.controller.user;

import com.nova.result.Result;
import com.nova.service.SetmealService;
import com.nova.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Tag(name = "用户端-套餐浏览接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @Operation(summary = "根据分类ID查询套餐")
    public Result<List<SetmealVO>> list(Long categoryId) {
        return Result.success(setmealService.listByCategoryId(categoryId));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "根据ID查询套餐详情")
    public Result<SetmealVO> detail(@PathVariable Long id) {
        return Result.success(setmealService.getById(id));
    }
}
