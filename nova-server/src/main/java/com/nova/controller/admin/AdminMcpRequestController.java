package com.nova.controller.admin;

import com.nova.entity.McpOrderRequest;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.McpRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 MCP 审批中心：分页查看待审批/历史申请，批准或驳回。
 */
@RestController("adminMcpRequestController")
@RequestMapping("/admin/mcp-request")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端-MCP 审批中心")
public class AdminMcpRequestController {

    private final McpRequestService mcpRequestService;

    @GetMapping("/page")
    @Operation(summary = "审批申请分页查询")
    public Result<PageResult> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            String status) {
        return Result.success(mcpRequestService.page(page, pageSize, status));
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "批准并执行")
    public Result<McpOrderRequest> approve(@PathVariable Long id) {
        return Result.success(mcpRequestService.approve(id));
    }

    @PutMapping("/reject/{id}")
    @Operation(summary = "驳回")
    public Result<McpOrderRequest> reject(@PathVariable Long id,
                                          @RequestParam(required = false) String reason) {
        return Result.success(mcpRequestService.reject(id, reason));
    }
}
