package com.nova.controller.user;

import com.nova.dto.OrdersPageQueryDTO;
import com.nova.dto.OrdersSubmitDTO;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.OrderService;
import com.nova.vo.OrderVO;
import com.nova.vo.OrdersSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户端-订单相关接口")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/idempotent-token")
    @Operation(summary = "获取幂等令牌，防止重复下单")
    public Result<String> getIdempotentToken() {
        String token = orderService.createIdempotentToken();
        return Result.success(token);
    }

    @PostMapping("/submit")
    @Operation(summary = "用户下单")
    public Result<OrdersSubmitVO> submit(@Valid @RequestBody OrdersSubmitDTO dto) {
        return Result.success(orderService.submitOrder(dto));
    }

    @PostMapping("/payment")
    @Operation(summary = "模拟支付")
    public Result payment(@RequestParam String orderNumber) {
        orderService.paySuccess(orderNumber);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消订单")
    public Result cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @Operation(summary = "催单")
    public Result reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }

    @GetMapping("/historyOrders")
    @Operation(summary = "历史订单查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO dto) {
        return Result.success(orderService.historyOrders(dto));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.userDetail(id));
    }
}
