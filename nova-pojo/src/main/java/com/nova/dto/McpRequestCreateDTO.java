package com.nova.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * MCP 创建交易审批请求的入参。
 * <p>
 * 用户身份由 JWT 解析（BaseContext），不在入参中传递；
 * {@code idempotencyKey} 用于 Agent 重试时避免产生多条待审批申请。
 */
@Data
public class McpRequestCreateDTO implements Serializable {

    /** 申请级幂等键（可选；不传则由服务端生成随机值）。 */
    private String idempotencyKey;
    /** 动作：SUBMIT / PAY / CANCEL / REMINDER。 */
    private String action;
    /** 来源工具名（审计用，可选）。 */
    private String toolName;

    // ---- 动作参数（按 action 取用）----
    /** SUBMIT：收货地址 ID。 */
    private Long addressBookId;
    /** SUBMIT：备注。 */
    private String remark;
    /** CANCEL / REMINDER：订单 ID。 */
    private Long orderId;
    /** PAY：订单号。 */
    private String orderNumber;
}
