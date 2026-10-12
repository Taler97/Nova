package com.nova.task;

import com.nova.entity.McpOrderRequest;
import com.nova.mapper.McpOrderRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP 审批超时清理：把超过 expires_at 仍为 PENDING 的申请置为 EXPIRED。
 * <p>
 * 现状项目无分布式锁，多实例部署会重复触发（幂等：markExpired 带 status 守卫，重复执行无副作用）。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McpRequestTask {

    private final McpOrderRequestMapper mapper;

    @Scheduled(fixedDelay = 60000)
    public void expirePendingRequests() {
        List<McpOrderRequest> expired = mapper.listExpired(LocalDateTime.now());
        for (McpOrderRequest request : expired) {
            int updated = mapper.markExpired(request.getId());
            if (updated > 0) {
                log.info("MCP 审批申请超时过期：{}", request.getRequestNo());
            }
        }
    }
}
