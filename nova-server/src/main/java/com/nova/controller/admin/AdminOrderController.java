package com.nova.controller.admin;

import com.nova.dto.OrdersConfirmDTO;
import com.nova.dto.OrdersRejectionDTO;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.OrderService;
import com.nova.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Tag(name = "管理端-订单管理接口")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @Operation(summary = "订单搜索")
    public Result<PageResult> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            String number, String phone, Integer status) {
        return Result.success(orderService.adminSearch(page, pageSize, number, phone, status));
    }

    @PutMapping("/confirm")
    @Operation(summary = "接单")
    public Result confirm(@RequestBody OrdersConfirmDTO dto) {
        orderService.confirm(dto);
        return Result.success();
    }

    @PutMapping("/rejection")
    @Operation(summary = "拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO dto) {
        orderService.rejection(dto);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @Operation(summary = "派送订单")
    public Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成订单")
    public Result complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.adminDetail(id));
    }
}
