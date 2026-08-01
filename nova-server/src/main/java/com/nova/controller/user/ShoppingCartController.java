package com.nova.controller.user;

import com.nova.dto.ShoppingCartDTO;
import com.nova.entity.ShoppingCart;
import com.nova.result.Result;
import com.nova.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("userShoppingCartController")
@RequestMapping("/user/shoppingCart")
@Slf4j
@Tag(name = "用户端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @Operation(summary = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO dto) {
        shoppingCartService.add(dto);
        return Result.success();
    }

    @PostMapping("/sub")
    @Operation(summary = "购物车减1")
    public Result sub(@RequestBody ShoppingCartDTO dto) {
        shoppingCartService.sub(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "查看购物车")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.list());
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清空购物车")
    public Result clean() {
        shoppingCartService.clean();
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "删除购物车中一个商品")
    public Result deleteById(Long id) {
        shoppingCartService.deleteById(id);
        return Result.success();
    }
}
