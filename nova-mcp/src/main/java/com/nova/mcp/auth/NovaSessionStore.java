package com.nova.mcp.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话身份存储（M1 过渡方案）。
 * <p>
 * 按 MCP 会话（{@code Mcp-Session-Id}）保存由 nova-server 签发的用户令牌，
 * 供后续工具以「该用户身份」调用交易接口。
 * <p>
 * <b>M1 的定位是过渡</b>：令牌仅存于内存、不落库；Phase 2 将由标准
 * Token Exchange（RFC 8693）取代（见 docs/10 §5.1）。
 * <p>
 * 已知限制：未做 TTL 自动清理，令牌过期由 nova-server 返回 401 时暴露；
 * 会话结束（{@code DELETE /mcp}）时的清理见 {@link #remove(String)}。
 */
@Component
public class NovaSessionStore {

    private final Map<String, String> tokenBySessionId = new ConcurrentHashMap<>();
    private final Map<String, String> idemTokenBySessionId = new ConcurrentHashMap<>();

    /** 绑定会话与用户令牌。 */
    public void bind(String sessionId, String token) {
        tokenBySessionId.put(sessionId, token);
    }

    /**
     * 取当前会话的用户令牌。
     *
     * @throws IllegalStateException 会话尚未登录时抛出
     */
    public String requireToken(String sessionId) {
        String token = sessionId == null ? null : tokenBySessionId.get(sessionId);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("当前会话尚未登录，请先调用 login_nova_account 登录");
        }
        return token;
    }

    /** 缓存幂等令牌到当前会话，供后续 submit_order 使用。 */
    public void cacheIdempotentToken(String sessionId, String idemToken) {
        if (sessionId != null && idemToken != null) {
            idemTokenBySessionId.put(sessionId, idemToken);
        }
    }

    /**
     * 取出并清除当前会话缓存的幂等令牌（一次性消费）。
     *
     * @throws IllegalStateException 未获取过幂等令牌时抛出
     */
    public String consumeIdempotentToken(String sessionId) {
        if (sessionId == null) {
            throw new IllegalStateException("请先调用 get_idempotent_token 获取幂等令牌");
        }
        String token = idemTokenBySessionId.remove(sessionId);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("请先调用 get_idempotent_token 获取幂等令牌");
        }
        return token;
    }

    /** 解绑会话（登出或会话结束）。 */
    public void remove(String sessionId) {
        if (sessionId != null) {
            tokenBySessionId.remove(sessionId);
            idemTokenBySessionId.remove(sessionId);
        }
    }

    /** 当前已登录会话数（用于观测）。 */
    public int size() {
        return tokenBySessionId.size();
    }
}
