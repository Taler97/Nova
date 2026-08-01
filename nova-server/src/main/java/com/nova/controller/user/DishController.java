package com.nova.controller.user;

import com.nova.result.Result;
import com.nova.service.DishService;
import com.nova.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Tag(name = "用户端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    @Operation(summary = "根据分类ID查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        return Result.success(dishService.list(categoryId));
    }
}
