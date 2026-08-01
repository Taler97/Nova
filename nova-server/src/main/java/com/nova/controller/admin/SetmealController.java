package com.nova.controller.admin;

import com.nova.dto.SetmealDTO;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.SetmealService;
import com.nova.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Tag(name = "套餐管理相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @Operation(summary = "新增套餐")
    public Result save(@RequestBody SetmealDTO dto) {
        setmealService.save(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return Result.success(setmealService.getById(id));
    }

    @PutMapping
    @Operation(summary = "修改套餐")
    public Result update(@RequestBody SetmealDTO dto) {
        setmealService.update(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult> page(SetmealDTO dto) {
        return Result.success(setmealService.pageQuery(dto));
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "起售/停售套餐")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    public Result deleteBatch(@RequestParam("ids") List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }
}
