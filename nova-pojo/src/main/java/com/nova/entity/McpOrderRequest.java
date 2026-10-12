package com.nova.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MCP 交易审批请求。
 * <p>
 * MCP 写操作不直接执行，而是落一条 PENDING 申请；人工批准后由执行器以
 * {@link #userId} 身份复用幂等令牌真正下单。状态机：
 * PENDING → APPROVED → EXECUTED / FAILED；或 PENDING → REJECTED / EXPIRED。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpOrderRequest implements Serializable {

    /** 待审批 */
    public static final String PENDING = "PENDING";
    /** 已批准（待执行/执行中） */
    public static final String APPROVED = "APPROVED";
    /** 已驳回 */
    public static final String REJECTED = "REJECTED";
    /** 已执行成功 */
    public static final String EXECUTED = "EXECUTED";
    /** 执行失败 */
    public static final String FAILED = "FAILED";
    /** 已过期 */
    public static final String EXPIRED = "EXPIRED";

    /** 动作：下单 */
    public static final String ACTION_SUBMIT = "SUBMIT";
    /** 动作：支付 */
    public static final String ACTION_PAY = "PAY";
    /** 动作：取消 */
    public static final String ACTION_CANCEL = "CANCEL";
    /** 动作：催单 */
    public static final String ACTION_REMINDER = "REMINDER";

    private Long id;
    private String requestNo;
    private Long userId;
    private String toolName;
    private String action;
    private String payloadJson;
    private String previewJson;
    private String status;
    private String mcpSessionId;
    private String principal;
    private String idempotencyKey;
    private String resultJson;
    private String rejectReason;
    private Long decidedBy;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime decidedAt;
}
