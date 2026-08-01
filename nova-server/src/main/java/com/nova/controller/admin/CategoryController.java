package com.nova.controller.admin;

import com.nova.dto.CategoryDTO;
import com.nova.dto.CategoryPageQueryDTO;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "分类管理相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/page")
    @Operation(summary = "分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO dto) {
        return Result.success(categoryService.pageQuery(dto));
    }

    @PostMapping
    @Operation(summary = "新增分类")
    public Result save(@Valid @RequestBody CategoryDTO dto) {
        categoryService.save(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改分类")
    public Result update(@Valid @RequestBody CategoryDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
        return Result.success();
    }
}
