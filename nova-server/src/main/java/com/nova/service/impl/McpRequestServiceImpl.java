package com.nova.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nova.context.BaseContext;
import com.nova.dto.McpRequestCreateDTO;
import com.nova.entity.AddressBook;
import com.nova.entity.Dish;
import com.nova.entity.McpOrderRequest;
import com.nova.entity.Setmeal;
import com.nova.entity.ShoppingCart;
import com.nova.exception.BaseException;
import com.nova.mapper.AddressBookMapper;
import com.nova.mapper.DishMapper;
import com.nova.mapper.McpOrderRequestMapper;
import com.nova.mapper.SetmealMapper;
import com.nova.mapper.ShoppingCartMapper;
import com.nova.result.PageResult;
import com.nova.service.McpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class McpRequestServiceImpl implements McpRequestService {

    private final McpOrderRequestMapper mapper;
    private final McpRequestExecutor executor;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    /** 审批有效期（分钟），超时自动 EXPIRED。 */
    private static final long TTL_MINUTES = 30;

    @Override
    public McpOrderRequest create(McpRequestCreateDTO dto, String mcpSessionId, String principal) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("未登录");
        }
        String action = dto.getAction();
        if (action == null || action.isBlank()) {
            throw new BaseException("action 不能为空");
        }
        if (!isSupportedAction(action)) {
            throw new BaseException("不支持的 action：" + action);
        }

        String idempotencyKey = (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().isBlank())
                ? dto.getIdempotencyKey()
                : UUID.randomUUID().toString().replace("-", "");

        // 幂等闸门：同 key 直接返回既有申请（Agent 重试不产生多条待审批）
        McpOrderRequest existing = mapper.getByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        LocalDateTime now = LocalDateTime.now();
        McpOrderRequest request = McpOrderRequest.builder()
                .requestNo(generateRequestNo())
                .userId(userId)
                .toolName(dto.getToolName())
                .action(action)
                .payloadJson(buildPayload(dto))
                .previewJson(buildPreview(userId, dto))
                .status(McpOrderRequest.PENDING)
                .mcpSessionId(mcpSessionId)
                .principal(principal)
                .idempotencyKey(idempotencyKey)
                .createdAt(now)
                .expiresAt(now.plusMinutes(TTL_MINUTES))
                .build();
        try {
            mapper.insert(request);
        } catch (DuplicateKeyException e) {
            // 并发下唯一约束兜底，返回已存在申请
            return mapper.getByIdempotencyKey(idempotencyKey);
        }
        return request;
    }

    @Override
    public McpOrderRequest getByRequestNo(String requestNo) {
        return mapper.getByRequestNo(requestNo);
    }

    @Override
    public PageResult page(int page, int pageSize, String status) {
        PageHelper.startPage(page > 0 ? page : 1, pageSize > 0 ? pageSize : 10);
        Page<McpOrderRequest> result = mapper.adminPage(status);
        return new PageResult(result.getTotal(), result.getResult());
    }

    @Override
    public McpOrderRequest approve(Long id) {
        McpOrderRequest request = mapper.getById(id);
        if (request == null) {
            throw new BaseException("审批申请不存在");
        }
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            mapper.markExpired(id);
            throw new BaseException("申请已过期，无法批准");
        }

        Long adminId = BaseContext.getCurrentId();
        int updated = mapper.markApproved(id, adminId, LocalDateTime.now());
        if (updated == 0) {
            throw new BaseException("申请状态已变更（可能已被处理），请刷新后重试");
        }

        // 执行真实业务：独立于状态流转，失败只影响终态
        String finalStatus;
        String resultJson;
        try {
            resultJson = executor.execute(request);
            finalStatus = McpOrderRequest.EXECUTED;
        } catch (Exception e) {
            finalStatus = McpOrderRequest.FAILED;
            resultJson = (e.getMessage() == null || e.getMessage().isBlank())
                    ? e.getClass().getSimpleName() : e.getMessage();
        }
        mapper.finish(id, finalStatus, resultJson);

        request.setStatus(finalStatus);
        request.setResultJson(resultJson);
        request.setDecidedBy(adminId);
        return request;
    }

    @Override
    public McpOrderRequest reject(Long id, String reason) {
        McpOrderRequest request = mapper.getById(id);
        if (request == null) {
            throw new BaseException("审批申请不存在");
        }
        Long adminId = BaseContext.getCurrentId();
        int updated = mapper.markRejected(id, adminId, LocalDateTime.now(), reason);
        if (updated == 0) {
            throw new BaseException("申请状态已变更（可能已被处理），请刷新后重试");
        }
        return mapper.getById(id);
    }

    // ------------------------------------------------------------------ 内部

    private boolean isSupportedAction(String action) {
        return McpOrderRequest.ACTION_SUBMIT.equals(action)
                || McpOrderRequest.ACTION_PAY.equals(action)
                || McpOrderRequest.ACTION_CANCEL.equals(action)
                || McpOrderRequest.ACTION_REMINDER.equals(action);
    }

    private String generateRequestNo() {
        String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "MR" + ts + rnd;
    }

    private String buildPayload(McpRequestCreateDTO dto) {
        JSONObject payload = new JSONObject();
        payload.put("action", dto.getAction());
        payload.put("addressBookId", dto.getAddressBookId());
        payload.put("remark", dto.getRemark());
        payload.put("orderId", dto.getOrderId());
        payload.put("orderNumber", dto.getOrderNumber());
        return payload.toJSONString();
    }

    private String buildPreview(Long userId, McpRequestCreateDTO dto) {
        JSONObject preview = new JSONObject();
        preview.put("action", dto.getAction());
        if (McpOrderRequest.ACTION_SUBMIT.equals(dto.getAction())) {
            AddressBook address = dto.getAddressBookId() == null ? null : addressBookMapper.getById(dto.getAddressBookId());
            preview.put("address", address == null ? null
                    : address.getConsignee() + " " + address.getPhone() + " " + address.getDetail());
            List<ShoppingCart> cart = shoppingCartMapper.list(userId);
            JSONArray items = new JSONArray();
            BigDecimal total = BigDecimal.ZERO;
            for (ShoppingCart c : cart) {
                JSONObject item = new JSONObject();
                item.put("name", resolveItemName(c));
                item.put("number", c.getNumber());
                item.put("amount", c.getAmount());
                items.add(item);
                if (c.getAmount() != null) {
                    total = total.add(c.getAmount());
                }
            }
            preview.put("items", items);
            preview.put("amount", total);
            preview.put("remark", dto.getRemark());
        } else if (McpOrderRequest.ACTION_PAY.equals(dto.getAction())) {
            preview.put("orderNumber", dto.getOrderNumber());
        } else {
            preview.put("orderId", dto.getOrderId());
        }
        return preview.toJSONString();
    }

    private String resolveItemName(ShoppingCart cart) {
        if (cart.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.getById(cart.getSetmealId());
            return setmeal == null ? "套餐#" + cart.getSetmealId() : setmeal.getName();
        }
        if (cart.getDishId() != null) {
            Dish dish = dishMapper.getById(cart.getDishId());
            return dish == null ? "菜品#" + cart.getDishId() : dish.getName();
        }
        return "未知商品";
    }
}
