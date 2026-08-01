package com.nova.service.impl;

import com.nova.constant.StatusConstant;
import com.nova.entity.Orders;
import com.nova.mapper.DishMapper;
import com.nova.mapper.ReportMapper;
import com.nova.mapper.SetmealMapper;
import com.nova.service.WorkbenchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkbenchServiceImpl implements WorkbenchService {

    private final ReportMapper reportMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public Map<String, Object> getTodayData() {
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        int totalOrders = reportMapper.countOrders(begin, end);
        int validOrders = reportMapper.countOrdersByStatus(Orders.COMPLETED, begin, end);
        Double turnover = reportMapper.sumByStatusAndTime(Orders.COMPLETED, begin, end);
        int newUsers = reportMapper.countNewUsers(begin, end);
        int dishCount = dishMapper.countByStatus(StatusConstant.ENABLE);
        int setmealCount = setmealMapper.countByStatus(StatusConstant.ENABLE);

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", totalOrders);
        data.put("validOrders", validOrders);
        data.put("turnover", turnover != null ? turnover : 0.0);
        data.put("newUsers", newUsers);
        data.put("dishCount", dishCount);
        data.put("setmealCount", setmealCount);
        return data;
    }
}
