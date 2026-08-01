package com.nova.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nova.context.BaseContext;
import com.nova.dto.OrdersConfirmDTO;
import com.nova.dto.OrdersPageQueryDTO;
import com.nova.dto.OrdersRejectionDTO;
import com.nova.dto.OrdersSubmitDTO;
import com.nova.entity.*;
import com.nova.exception.OrderBusinessException;
import com.nova.mapper.*;
import com.nova.result.PageResult;
import com.nova.service.OrderService;
import com.nova.utils.AliOssUtil;
import com.nova.vo.OrderVO;
import com.nova.vo.OrdersSubmitVO;
import com.nova.enumeration.OrderStatus;
import com.nova.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final AliOssUtil aliOssUtil;
    private final WebSocketServer webSocketServer;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:order:";
    private static final long IDEMPOTENT_TOKEN_TTL_MINUTES = 5;

    @Override
    @Transactional
    public OrdersSubmitVO submitOrder(OrdersSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 幂等校验：原子消费令牌
        String tokenKey = IDEMPOTENT_KEY_PREFIX + dto.getIdempotentToken();
        Boolean deleted = stringRedisTemplate.delete(tokenKey);
        if (Boolean.FALSE.equals(deleted)) {
            throw new OrderBusinessException("请勿重复提交订单");
        }

        AddressBook addressBook = addressBookMapper.getById(dto.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException("地址不存在");
        }

        List<ShoppingCart> cartList = shoppingCartMapper.list(userId);
        if (cartList == null || cartList.isEmpty()) {
            throw new OrderBusinessException("购物车为空");
        }

        BigDecimal total = cartList.stream()
                .map(ShoppingCart::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Orders order = new Orders();
        order.setNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setUserId(userId);
        order.setAmount(total);
        order.setOrderTime(LocalDateTime.now());
        order.setPayMethod(1);
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setRemark(dto.getRemark());
        order.setEstimatedDeliveryTime(dto.getEstimatedDeliveryTime());
        orderMapper.insert(order);

        List<OrderDetail> details = cartList.stream().map(c -> {
            OrderDetail od = new OrderDetail();
            od.setOrderId(order.getId());
            od.setDishId(c.getDishId());
            od.setNumber(c.getNumber());
            od.setAmount(c.getAmount());
            od.setDishFlavor(c.getDishFlavor());
            if (c.getSetmealId() != null) {
                Setmeal setmeal = setmealMapper.getById(c.getSetmealId());
                if (setmeal != null) {
                    od.setName(setmeal.getName());
                    od.setImage(setmeal.getImage());
                }
            } else if (c.getDishId() != null) {
                Dish dish = dishMapper.getById(c.getDishId());
                if (dish != null) {
                    od.setName(dish.getName());
                    od.setImage(dish.getImage());
                }
            }
            return od;
        }).collect(Collectors.toList());
        orderDetailMapper.insertBatch(details);

        shoppingCartMapper.cleanByUserId(userId);

        JSONObject msg = new JSONObject();
        msg.put("type", 1);
        msg.put("orderId", order.getId());
        msg.put("content", "来单提醒：订单号 " + order.getNumber());
        webSocketServer.sendToAll(msg.toJSONString());

        return OrdersSubmitVO.builder()
                .id(order.getId())
                .number(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    @Override
    public String createIdempotentToken() {
        Long userId = BaseContext.getCurrentId();
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = IDEMPOTENT_KEY_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(userId), IDEMPOTENT_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public void paySuccess(String orderNumber) {
        Orders order = orderMapper.getByNumber(orderNumber);
        if (order != null) {
            order.setStatus(Orders.TO_BE_CONFIRMED);
            orderMapper.update(order);
        }
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        if (order.getStatus() == Orders.COMPLETED) {
            throw new OrderBusinessException("已完成订单无法取消");
        }
        if (order.getStatus() == Orders.CANCELLED) {
            throw new OrderBusinessException("订单已取消，请勿重复操作");
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("用户取消");
        orderMapper.update(order);
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 数据库层真分页：PageHelper + 带状态过滤的 SQL
        PageHelper.startPage(
                dto.getPage() > 0 ? dto.getPage() : 1,
                dto.getPageSize() > 0 ? dto.getPageSize() : 10);
        Page<Orders> page = orderMapper.listByUserIdAndStatus(userId, dto.getStatus());
        List<Orders> orders = page.getResult();

        // 批量查当前页订单明细
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        Map<Long, List<OrderDetail>> detailsMap;
        if (!orderIds.isEmpty()) {
            detailsMap = orderDetailMapper.getByOrderIds(orderIds).stream()
                    .peek(d -> d.setImage(aliOssUtil.convertToSignedUrl(d.getImage())))
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));
        } else {
            detailsMap = Collections.emptyMap();
        }

        List<OrderVO> vos = orders.stream().map(o ->
                OrderVO.builder()
                        .id(o.getId())
                        .number(o.getNumber())
                        .status(o.getStatus())
                        .userId(o.getUserId())
                        .amount(o.getAmount())
                        .orderTime(o.getOrderTime())
                        .payMethod(o.getPayMethod())
                        .consignee(o.getConsignee())
                        .phone(o.getPhone())
                        .address(o.getAddress())
                        .remark(o.getRemark())
                        .estimatedDeliveryTime(o.getEstimatedDeliveryTime())
                        .orderDetails(detailsMap.getOrDefault(o.getId(), Collections.emptyList()))
                        .build()
        ).collect(Collectors.toList());

        return new PageResult(page.getTotal(), vos);
    }

    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        JSONObject msg = new JSONObject();
        msg.put("type", 2);
        msg.put("orderId", order.getId());
        msg.put("content", "催单提醒：订单号 " + order.getNumber());
        webSocketServer.sendToAll(msg.toJSONString());
    }

    @Override
    public OrderVO userDetail(Long id) {
        Long userId = BaseContext.getCurrentId();
        Orders order = orderMapper.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new OrderBusinessException("订单不存在");
        }
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        details.forEach(d -> d.setImage(aliOssUtil.convertToSignedUrl(d.getImage())));
        return toOrderVO(order, details);
    }

    @Override
    public OrderVO adminDetail(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        return toOrderVO(order, details);
    }

    private OrderVO toOrderVO(Orders order, List<OrderDetail> details) {
        return OrderVO.builder()
                .id(order.getId())
                .number(order.getNumber())
                .status(order.getStatus())
                .userId(order.getUserId())
                .amount(order.getAmount())
                .orderTime(order.getOrderTime())
                .payMethod(order.getPayMethod())
                .consignee(order.getConsignee())
                .phone(order.getPhone())
                .address(order.getAddress())
                .remark(order.getRemark())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .orderDetails(details)
                .build();
    }

    @Override
    public PageResult adminSearch(Integer page, Integer pageSize, String number, String phone, Integer status) {
        PageHelper.startPage(page, pageSize);
        Page<Orders> result = orderMapper.adminSearch(number, phone, status);
        return new PageResult(result.getTotal(), result.getResult());
    }

    @Override
    public void confirm(OrdersConfirmDTO dto) {
        Orders order = orderMapper.getById(dto.getId());
        if (order == null || !order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException("订单状态错误");
        }
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO dto) {
        Orders order = orderMapper.getById(dto.getId());
        if (order == null || !order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException("订单状态错误");
        }
        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(dto.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void delivery(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null || !order.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException("订单状态错误");
        }
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    @Override
    public void complete(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null || !order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException("订单状态错误");
        }
        order.setStatus(Orders.COMPLETED);
        orderMapper.update(order);
    }
}
