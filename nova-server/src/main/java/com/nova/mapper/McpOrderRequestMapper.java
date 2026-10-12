package com.nova.mapper;

import com.github.pagehelper.Page;
import com.nova.entity.McpOrderRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface McpOrderRequestMapper {

    /** 新增申请（回填自增 id）。 */
    void insert(McpOrderRequest request);

    /** 管理端分页（status 为空表示全部）。 */
    Page<McpOrderRequest> adminPage(String status);

    @Select("SELECT * FROM mcp_order_request WHERE id = #{id}")
    McpOrderRequest getById(Long id);

    @Select("SELECT * FROM mcp_order_request WHERE request_no = #{requestNo}")
    McpOrderRequest getByRequestNo(String requestNo);

    @Select("SELECT * FROM mcp_order_request WHERE idempotency_key = #{idempotencyKey}")
    McpOrderRequest getByIdempotencyKey(String idempotencyKey);

    @Select("SELECT * FROM mcp_order_request WHERE status = 'PENDING' AND expires_at < #{now}")
    List<McpOrderRequest> listExpired(LocalDateTime now);

    // ------- 原子状态流转（带 status 守卫，避免并发重复处理）-------

    @Update("UPDATE mcp_order_request SET status = 'APPROVED', decided_by = #{decidedBy}, decided_at = #{decidedAt} " +
            "WHERE id = #{id} AND status = 'PENDING'")
    int markApproved(@Param("id") Long id, @Param("decidedBy") Long decidedBy, @Param("decidedAt") LocalDateTime decidedAt);

    @Update("UPDATE mcp_order_request SET status = 'REJECTED', decided_by = #{decidedBy}, decided_at = #{decidedAt}, " +
            "reject_reason = #{reason} WHERE id = #{id} AND status = 'PENDING'")
    int markRejected(@Param("id") Long id, @Param("decidedBy") Long decidedBy,
                     @Param("decidedAt") LocalDateTime decidedAt, @Param("reason") String reason);

    @Update("UPDATE mcp_order_request SET status = 'EXPIRED' WHERE id = #{id} AND status = 'PENDING'")
    int markExpired(@Param("id") Long id);

    /** 执行结束后写回终态与结果。 */
    @Update("UPDATE mcp_order_request SET status = #{status}, result_json = #{resultJson} WHERE id = #{id}")
    int finish(@Param("id") Long id, @Param("status") String status, @Param("resultJson") String resultJson);
}
