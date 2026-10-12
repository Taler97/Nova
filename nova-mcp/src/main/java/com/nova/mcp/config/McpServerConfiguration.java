package com.nova.mcp.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.nova.mcp.auth.NovaSessionStore;
import com.nova.mcp.client.NovaApiClient;
import com.nova.mcp.client.NovaApiException;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP Server 配置：Streamable HTTP（Servlet）+ 首批交易只读工具。
 * <p>
 * 工具清单（M0）：
 * <ul>
 *   <li>{@code login_nova_account} —— M1 过渡身份，登录后令牌绑定当前 MCP 会话</li>
 *   <li>{@code list_my_orders} —— 我的历史订单</li>
 *   <li>{@code get_order_detail} —— 订单详情</li>
 *   <li>{@code list_my_addresses} —— 我的收货地址</li>
 * </ul>
 */
@Configuration
@Slf4j
public class McpServerConfiguration {

    private static final String MCP_ENDPOINT = "/mcp";

    private static final Map<Integer, String> ORDER_STATUS = Map.of(
            1, "待付款", 2, "待接单", 3, "已接单", 4, "配送中", 5, "已完成", 6, "已取消");

    @Bean
    public McpJsonMapper mcpJsonMapper(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new JacksonMcpJsonMapper(objectMapper);
    }

    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransportProvider(McpJsonMapper mcpJsonMapper) {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(mcpJsonMapper)
                .mcpEndpoint(MCP_ENDPOINT)
                .build();
    }

    @Bean
    public ServletRegistrationBean<HttpServletStreamableServerTransportProvider> mcpServlet(
            HttpServletStreamableServerTransportProvider transportProvider) {
        return new ServletRegistrationBean<>(transportProvider, MCP_ENDPOINT, MCP_ENDPOINT + "/*");
    }

    @Bean(destroyMethod = "close")
    public McpSyncServer mcpSyncServer(HttpServletStreamableServerTransportProvider transportProvider,
                                       NovaApiClient novaApi,
                                       NovaSessionStore sessionStore) {
        return McpServer.sync(transportProvider)
                .serverInfo("nova-mcp", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tool(loginTool(), loginHandler(novaApi, sessionStore))
                .tool(listOrdersTool(), listOrdersHandler(novaApi, sessionStore))
                .tool(orderDetailTool(), orderDetailHandler(novaApi, sessionStore))
                .tool(listAddressesTool(), listAddressesHandler(novaApi, sessionStore))
                .tool(addCartTool(), addCartHandler(novaApi, sessionStore))
                .tool(idempotentTokenTool(), idempotentTokenHandler(novaApi, sessionStore))
                .tool(submitOrderTool(), submitOrderHandler(novaApi, sessionStore))
                .tool(submitOrderRequestTool(), submitOrderRequestHandler(novaApi, sessionStore))
                .tool(orderRequestStatusTool(), orderRequestStatusHandler(novaApi, sessionStore))
                .build();
    }

    // ------------------------------------------------------------- 工具定义

    private McpSchema.Tool loginTool() {
        return tool("login_nova_account",
                "以 Nova 用户账号（手机号 + 密码）登录，登录结果绑定到当前 MCP 会话。调用其它交易工具前需先登录。",
                Map.of(
                        "phone", Map.of("type", "string", "description", "手机号，例如 13800138000"),
                        "password", Map.of("type", "string", "description", "账号密码")),
                List.of("phone", "password"));
    }

    private McpSchema.Tool listOrdersTool() {
        return tool("list_my_orders",
                "查询当前登录用户的历史订单（分页）。",
                Map.of(
                        "page", Map.of("type", "integer", "description", "页码，从 1 开始，默认 1"),
                        "pageSize", Map.of("type", "integer", "description", "每页条数，默认 10"),
                        "status", Map.of("type", "integer",
                                "description", "订单状态过滤：1待付款 2待接单 3已接单 4配送中 5已完成 6已取消；不传表示全部")),
                List.of());
    }

    private McpSchema.Tool orderDetailTool() {
        return tool("get_order_detail",
                "查询当前登录用户某笔订单的详情（含菜品明细）。",
                Map.of("orderId", Map.of("type", "integer", "description", "订单 ID")),
                List.of("orderId"));
    }

    private McpSchema.Tool listAddressesTool() {
        return tool("list_my_addresses",
                "查询当前登录用户的收货地址列表，用于下单前选择地址。",
                Map.of(),
                List.of());
    }

    private McpSchema.Tool addCartTool() {
        return tool("add_to_cart",
                "向当前登录用户的购物车添加一份菜品。下单前需先调用此工具把菜品加入购物车。",
                Map.of("dishId", Map.of("type", "integer", "description", "菜品 ID，例如 1 表示鱼香肉丝")),
                List.of("dishId"));
    }

    private McpSchema.Tool idempotentTokenTool() {
        return tool("get_idempotent_token",
                "获取下单幂等令牌，防止重复提交。提交订单前必须先调用此工具获取令牌。",
                Map.of(),
                List.of());
    }

    private McpSchema.Tool submitOrderTool() {
        return tool("submit_order",
                "提交订单（需先登录、添加购物车、获取幂等令牌、并知道收货地址 ID）。",
                Map.of(
                        "addressBookId", Map.of("type", "integer", "description", "收货地址 ID，可通过 list_my_addresses 获取"),
                        "remark", Map.of("type", "string", "description", "订单备注，例如少辣、不要香菜，可留空")),
                List.of("addressBookId"));
    }

    private McpSchema.Tool submitOrderRequestTool() {
        return tool("submit_order_request",
                "提交下单申请（写入审批队列，需人工审批后才真正下单）。写操作推荐走此工具。",
                Map.of(
                        "addressBookId", Map.of("type", "integer", "description", "收货地址 ID，可通过 list_my_addresses 获取"),
                        "remark", Map.of("type", "string", "description", "订单备注，可留空"),
                        "idempotencyKey", Map.of("type", "string", "description", "幂等键（可选）。Agent 重试时携带同一值可避免重复申请")),
                List.of("addressBookId"));
    }

    private McpSchema.Tool orderRequestStatusTool() {
        return tool("get_order_request_status",
                "查询下单申请单的审批/执行状态（PENDING/APPROVED/EXECUTED/REJECTED/FAILED/EXPIRED）。",
                Map.of("requestNo", Map.of("type", "string", "description", "申请单号")),
                List.of("requestNo"));
    }

    private McpSchema.Tool tool(String name, String description, Map<String, Object> properties, List<String> required) {
        McpSchema.JsonSchema schema = new McpSchema.JsonSchema(
                "object", properties, required, Boolean.FALSE, null, null);
        return McpSchema.Tool.builder()
                .name(name)
                .description(description)
                .inputSchema(schema)
                .build();
    }

    // ------------------------------------------------------------- 工具实现

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> loginHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String phone = str(args, "phone");
            String password = str(args, "password");
            if (phone == null || password == null) {
                throw new NovaApiException("phone 与 password 均为必填");
            }
            String token = novaApi.webLogin(phone, password);
            sessionStore.bind(exchange.sessionId(), token);
            return new ArrayList<>(List.of("登录成功，当前会话已绑定用户 " + phone + "，可以查询订单与地址。"));
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> listOrdersHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            int page = intOr(args, "page", 1);
            int pageSize = intOr(args, "pageSize", 10);
            Integer status = args != null && args.get("status") != null ? intOr(args, "status", 0) : null;

            JsonNode data = novaApi.listOrders(token, page, pageSize, status);
            JsonNode records = data.path("records");
            long total = data.path("total").asLong(0);

            if (!records.isArray() || records.isEmpty()) {
                return new ArrayList<>(List.of("暂无订单记录。"));
            }
            List<String> lines = new ArrayList<>();
            lines.add("共 " + total + " 笔订单，当前第 " + page + " 页显示 " + records.size() + " 笔：");
            int i = 1;
            for (JsonNode o : records) {
                lines.add(i++ + ") 订单ID " + o.path("id").asLong()
                        + " | 单号 " + o.path("number").asText()
                        + " | 状态 " + statusText(o.path("status").asInt())
                        + " | 金额 ¥" + o.path("amount").asText()
                        + " | 下单时间 " + o.path("orderTime").asText()
                        + " | 收货人 " + o.path("consignee").asText());
            }
            return new ArrayList<>(lines);
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> orderDetailHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            int orderId = intOr(args, "orderId", 0);
            if (orderId <= 0) {
                throw new NovaApiException("orderId 必填且需为正整数");
            }
            JsonNode o = novaApi.getOrderDetail(token, orderId);

            List<String> lines = new ArrayList<>();
            lines.add("订单ID：" + o.path("id").asLong());
            lines.add("单号：" + o.path("number").asText());
            lines.add("状态：" + statusText(o.path("status").asInt()));
            lines.add("金额：¥" + o.path("amount").asText());
            lines.add("下单时间：" + o.path("orderTime").asText());
            lines.add("收货人：" + o.path("consignee").asText() + " " + o.path("phone").asText());
            lines.add("收货地址：" + o.path("address").asText());
            if (!o.path("remark").isMissingNode() && !o.path("remark").isNull()) {
                lines.add("备注：" + o.path("remark").asText());
            }
            JsonNode details = o.path("orderDetails");
            if (details.isArray() && !details.isEmpty()) {
                lines.add("菜品明细：");
                int i = 1;
                for (JsonNode d : details) {
                    lines.add("  " + i++ + ". " + d.path("name").asText()
                            + " x" + d.path("number").asInt()
                            + " 小计 ¥" + d.path("amount").asText()
                            + (d.path("dishFlavor").isMissingNode() || d.path("dishFlavor").isNull()
                                    ? "" : " 口味：" + d.path("dishFlavor").asText()));
                }
            }
            return new ArrayList<>(lines);
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> listAddressesHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            JsonNode data = novaApi.listAddresses(token);
            if (!data.isArray() || data.isEmpty()) {
                return new ArrayList<>(List.of("暂无收货地址，请先在客户端添加。"));
            }
            List<String> lines = new ArrayList<>();
            lines.add("共 " + data.size() + " 条收货地址：");
            int i = 1;
            for (JsonNode a : data) {
                boolean isDefault = a.path("isDefault").asInt(0) == 1;
                lines.add(i++ + ") 地址ID " + a.path("id").asLong()
                        + " | 收货人 " + a.path("consignee").asText()
                        + " | 电话 " + a.path("phone").asText()
                        + " | 地址 " + a.path("detail").asText()
                        + (isDefault ? " | 【默认】" : ""));
            }
            return new ArrayList<>(lines);
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> addCartHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            Object dishIdObj = args.get("dishId");
            if (dishIdObj == null) {
                throw new NovaApiException("dishId 必填");
            }
            long dishId = Long.parseLong(String.valueOf(dishIdObj));
            novaApi.addCart(token, dishId);
            return new ArrayList<>(List.of("已将菜品 " + dishId + " 加入购物车。"));
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> idempotentTokenHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            String idemToken = novaApi.getIdempotentToken(token);
            sessionStore.cacheIdempotentToken(exchange.sessionId(), idemToken);
            return new ArrayList<>(List.of("已获取幂等令牌并缓存到当前会话，接下来可调用 submit_order 提交订单。"));
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> submitOrderHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            Object addrIdObj = args.get("addressBookId");
            if (addrIdObj == null) {
                throw new NovaApiException("addressBookId 必填");
            }
            long addressBookId = Long.parseLong(String.valueOf(addrIdObj));
            String remark = str(args, "remark");
            String idemToken = sessionStore.consumeIdempotentToken(exchange.sessionId());
            String orderNumber = novaApi.submitOrder(token, addressBookId, remark, idemToken);
            return new ArrayList<>(List.of("下单成功！订单号：" + orderNumber
                    + (orderNumber == null ? "（服务端未返回订单号）" : "")));
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> submitOrderRequestHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            Object addrIdObj = args.get("addressBookId");
            if (addrIdObj == null) {
                throw new NovaApiException("addressBookId 必填");
            }
            long addressBookId = Long.parseLong(String.valueOf(addrIdObj));
            Map<String, Object> body = new HashMap<>();
            body.put("action", "SUBMIT");
            body.put("toolName", "submit_order_request");
            body.put("addressBookId", addressBookId);
            body.put("remark", str(args, "remark"));
            body.put("idempotencyKey", str(args, "idempotencyKey"));
            JsonNode data = novaApi.createApprovalRequest(token, body, exchange.sessionId(), null);
            String requestNo = data.path("requestNo").asText();
            return new ArrayList<>(List.of("已提交下单申请（申请单号 " + requestNo
                    + "），等待人工审批。可用 get_order_request_status 查询进度。"));
        });
    }

    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> orderRequestStatusHandler(
            NovaApiClient novaApi, NovaSessionStore sessionStore) {
        return (exchange, args) -> guard(() -> {
            String token = sessionStore.requireToken(exchange.sessionId());
            String requestNo = str(args, "requestNo");
            if (requestNo == null || requestNo.isBlank()) {
                throw new NovaApiException("requestNo 必填");
            }
            JsonNode data = novaApi.getApprovalRequest(token, requestNo);
            if (data == null || data.isMissingNode() || data.isNull()) {
                return new ArrayList<>(List.of("未找到申请单 " + requestNo));
            }
            String status = data.path("status").asText();
            String result = data.path("resultJson").asText(null);
            String rejectReason = data.path("rejectReason").asText(null);
            StringBuilder sb = new StringBuilder("申请单 " + requestNo + " 状态：" + status);
            if (result != null && !result.isBlank()) {
                sb.append("；结果：").append(result);
            }
            if (rejectReason != null && !rejectReason.isBlank()) {
                sb.append("；驳回原因：").append(rejectReason);
            }
            return new ArrayList<>(List.of(sb.toString()));
        });
    }

    // ------------------------------------------------------------- 工具辅助

    /**
     * 统一异常兜底：业务异常转可读文本并标记 isError，避免把堆栈暴露给模型。
     */
    private McpSchema.CallToolResult guard(ToolBody body) {
        try {
            List<String> lines = body.run();
            McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
            lines.forEach(builder::addTextContent);
            return builder.build();
        } catch (NovaApiException | IllegalStateException e) {
            log.warn("工具执行失败：{}", e.getMessage());
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("工具执行异常", e);
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("工具执行失败：" + e.getMessage())
                    .build();
        }
    }

    @FunctionalInterface
    private interface ToolBody {
        List<String> run();
    }

    private static String statusText(int code) {
        return ORDER_STATUS.getOrDefault(code, "未知(" + code + ")") + "(" + code + ")";
    }

    private static String str(Map<String, Object> args, String key) {
        Object v = args == null ? null : args.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static int intOr(Map<String, Object> args, String key, int defaultValue) {
        Object v = args == null ? null : args.get(key);
        if (v == null) {
            return defaultValue;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 保留：便于后续扩展 JSON 结构化输出。 */
    @SuppressWarnings("unused")
    private static Map<String, Object> ordered(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }
}
