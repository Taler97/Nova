package com.nova.controller.mcp;

import com.nova.dto.McpRequestCreateDTO;
import com.nova.entity.McpOrderRequest;
import com.nova.result.Result;
import com.nova.service.McpRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * MCP 桥接接口：由 nova-mcp 以「用户身份」（JWT）调用，提交交易审批申请与查询状态。
 * <p>
 * 与 /user/** 一样走用户端 JWT 拦截器，用户身份取自令牌，不在入参中传递。
 */
@RestController
@RequestMapping("/mcp-bridge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MCP 桥接-交易审批申请")
public class McpBridgeController {

    private final McpRequestService mcpRequestService;

    @PostMapping("/request")
    @Operation(summary = "创建交易审批申请（写操作不直接执行）")
    public Result<McpOrderRequest> create(
            @RequestBody McpRequestCreateDTO dto,
            @RequestHeader(value = "X-Mcp-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-Mcp-Principal", required = false) String principal) {
        return Result.success(mcpRequestService.create(dto, sessionId, principal));
    }

    @GetMapping("/request/{requestNo}")
    @Operation(summary = "查询审批/执行状态")
    public Result<McpOrderRequest> status(@PathVariable String requestNo) {
        return Result.success(mcpRequestService.getByRequestNo(requestNo));
    }
}
