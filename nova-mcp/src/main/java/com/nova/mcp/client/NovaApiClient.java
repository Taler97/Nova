package com.nova.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * nova-server REST 客户端。
 * <p>
 * M0 阶段以「用户身份」调用既有用户端接口：登录换取 JWT 后，用该 JWT 调用交易只读接口。
 * 统一处理 nova-server 的响应信封 {@code {code, msg, data}}。
 */
@Component
public class NovaApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public NovaApiClient(ObjectMapper objectMapper, @Value("${nova.server.base-url}") String baseUrl) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    /** 手机号 + 密码登录，返回 nova-server 签发的用户令牌。 */
    public String webLogin(String phone, String password) {
        JsonNode data = requireData(exchange(HttpMethod.POST, url("/user/user/webLogin"),
                Map.of("phone", phone, "password", password), null));
        String token = data.path("token").asText(null);
        if (token == null || token.isBlank()) {
            throw new NovaApiException("登录失败：服务端未返回令牌");
        }
        return token;
    }

    /** 历史订单查询（分页）。 */
    public JsonNode listOrders(String token, int page, int pageSize, Integer status) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url("/user/order/historyOrders"))
                .queryParam("page", page)
                .queryParam("pageSize", pageSize);
        if (status != null) {
            builder.queryParam("status", status);
        }
        return requireData(exchange(HttpMethod.GET, builder.toUriString(), null, token));
    }

    /** 订单详情。 */
    public JsonNode getOrderDetail(String token, long orderId) {
        return requireData(exchange(HttpMethod.GET, url("/user/order/detail/" + orderId), null, token));
    }

    /** 当前用户的收货地址列表。 */
    public JsonNode listAddresses(String token) {
        return requireData(exchange(HttpMethod.GET, url("/user/addressBook/list"), null, token));
    }

    /** 添加菜品到购物车。 */
    public void addCart(String token, Long dishId) {
        requireData(exchange(HttpMethod.POST, url("/user/shoppingCart/add"),
                Map.of("dishId", dishId), token));
    }

    /** 获取下单幂等令牌。 */
    public String getIdempotentToken(String token) {
        JsonNode data = requireData(exchange(HttpMethod.GET, url("/user/order/idempotent-token"), null, token));
        return data.asText(null);
    }

    /** 提交订单，返回订单号。 */
    public String submitOrder(String token, Long addressBookId, String remark, String idempotentToken) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("addressBookId", addressBookId);
        body.put("idempotentToken", idempotentToken);
        if (remark != null && !remark.isBlank()) {
            body.put("remark", remark);
        }
        JsonNode data = requireData(exchange(HttpMethod.POST, url("/user/order/submit"), body, token));
        return data.path("number").asText(null);
    }

    /** 创建 MCP 交易审批申请（写操作不直接执行，需人工审批）。 */
    public JsonNode createApprovalRequest(String token, java.util.Map<String, Object> body,
                                          String sessionId, String principal) {
        java.util.Map<String, String> extra = new java.util.HashMap<>();
        if (sessionId != null && !sessionId.isBlank()) {
            extra.put("X-Mcp-Session-Id", sessionId);
        }
        if (principal != null && !principal.isBlank()) {
            extra.put("X-Mcp-Principal", principal);
        }
        return requireData(exchange(HttpMethod.POST, url("/mcp-bridge/request"), body, token, extra));
    }

    /** 查询 MCP 审批申请状态。 */
    public JsonNode getApprovalRequest(String token, String requestNo) {
        return requireData(exchange(HttpMethod.GET, url("/mcp-bridge/request/" + requestNo), null, token));
    }

    // ------------------------------------------------------------------ 内部

    private String url(String path) {
        return baseUrl + path;
    }

    private JsonNode exchange(HttpMethod method, String fullUrl, Object body, String token) {
        return exchange(method, fullUrl, body, token, null);
    }

    private JsonNode exchange(HttpMethod method, String fullUrl, Object body, String token,
                              java.util.Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        if (token != null && !token.isBlank()) {
            // nova-server 用户端的鉴权请求头
            headers.set("authentication", token);
        }
        if (extraHeaders != null) {
            extraHeaders.forEach(headers::set);
        }
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, method, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (ResourceAccessException e) {
            throw new NovaApiException("无法连接 nova-server（" + baseUrl + "），请确认服务已启动", e);
        } catch (RestClientException e) {
            throw new NovaApiException("调用 nova-server 失败：" + e.getMessage(), e);
        } catch (Exception e) {
            throw new NovaApiException("解析 nova-server 响应失败：" + e.getMessage(), e);
        }
    }

    /** 校验响应信封 code=1，返回 data 节点。 */
    private JsonNode requireData(JsonNode envelope) {
        if (envelope == null) {
            throw new NovaApiException("nova-server 返回空响应");
        }
        int code = envelope.path("code").asInt(0);
        if (code != 1) {
            String msg = envelope.path("msg").asText("未知错误");
            throw new NovaApiException(msg);
        }
        return envelope.path("data");
    }
}
