package com.nova.controller.admin;

import com.nova.dto.DishDTO;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.DishService;
import com.nova.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Tag(name = "菜品管理相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @Operation(summary = "新增菜品")
    public Result save(@RequestBody DishDTO dto) {
        dishService.save(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    @PutMapping
    @Operation(summary = "修改菜品")
    public Result update(@RequestBody DishDTO dto) {
        dishService.update(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "菜品分页查询")
    public Result<PageResult> page(DishDTO dto) {
        return Result.success(dishService.pageQuery(dto));
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "起售/停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除菜品")
    public Result deleteBatch(@RequestParam("ids") List<Long> ids) {
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据分类ID查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        return Result.success(dishService.list(categoryId));
    }
}
