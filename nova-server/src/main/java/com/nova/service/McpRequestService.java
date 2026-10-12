package com.nova.service;

import com.nova.dto.McpRequestCreateDTO;
import com.nova.entity.McpOrderRequest;
import com.nova.result.PageResult;

/**
 * MCP 交易审批闸门：写操作先落「待审批申请」，人工批准后才真正执行。
 */
public interface McpRequestService {

    /** 创建审批申请（幂等；同 idempotencyKey 直接返回既有申请）。 */
    McpOrderRequest create(McpRequestCreateDTO dto, String mcpSessionId, String principal);

    /** 按申请单号查询。 */
    McpOrderRequest getByRequestNo(String requestNo);

    /** 管理端分页（status 为空表示全部）。 */
    PageResult page(int page, int pageSize, String status);

    /** 批准并执行；返回执行后的申请单。 */
    McpOrderRequest approve(Long id);

    /** 驳回。 */
    McpOrderRequest reject(Long id, String reason);
}
