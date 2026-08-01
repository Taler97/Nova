package com.nova.controller.admin;

import com.nova.mapper.ReportMapper;
import com.nova.result.Result;
import com.nova.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController("reportController")
@RequestMapping("/admin/report")
@Slf4j
@Tag(name = "管理端-数据统计接口")
public class ReportController {

    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ReportService reportService;

    /**
     * 将分组查询结果填充到日期范围内，缺失日期的值补 defaultVal
     */
    private <T> void fillDateRange(LocalDate begin, LocalDate end,
                                    Map<LocalDate, T> dataMap,
                                    List<String> dateList, List<T> valueList,
                                    T defaultVal) {
        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            dateList.add(d.toString());
            valueList.add(dataMap.getOrDefault(d, defaultVal));
        }
    }

    @GetMapping("/turnoverStatistics")
    @Operation(summary = "营业额统计")
    public Result turnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 一次分组查询替代逐日遍历
        List<Map<String, Object>> rows = reportMapper.sumTurnoverGroupByDay(
                com.nova.entity.Orders.COMPLETED, beginTime, endTime);
        Map<LocalDate, Double> turnoverMap = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((java.sql.Date) r.get("day")).toLocalDate(),
                        r -> ((Number) r.get("total")).doubleValue()));

        List<String> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        fillDateRange(begin, end, turnoverMap, dateList, turnoverList, 0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("dateList", dateList);
        result.put("turnoverList", turnoverList);
        return Result.success(result);
    }

    @GetMapping("/userStatistics")
    @Operation(summary = "用户统计")
    public Result userStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 一次分组查询新增用户数
        List<Map<String, Object>> rows = reportMapper.countNewUsersGroupByDay(beginTime, endTime);
        Map<LocalDate, Integer> newUserMap = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((java.sql.Date) r.get("day")).toLocalDate(),
                        r -> ((Number) r.get("total")).intValue()));

        List<String> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        fillDateRange(begin, end, newUserMap, dateList, newUserList, 0);

        // 累计用户：从 newUserList 累加，起始为 begin 前总用户数
        int cumulative = Optional.ofNullable(
                reportMapper.countTotalUsers(beginTime)).orElse(0);
        List<Integer> totalUserList = new ArrayList<>();
        for (Integer daily : newUserList) {
            cumulative += daily;
            totalUserList.add(cumulative);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dateList", dateList);
        result.put("newUserList", newUserList);
        result.put("totalUserList", totalUserList);
        return Result.success(result);
    }

    @GetMapping("/ordersStatistics")
    @Operation(summary = "订单统计")
    public Result ordersStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 两次分组查询替代 2*N 次查询
        List<Map<String, Object>> orderRows = reportMapper.countOrdersGroupByDay(beginTime, endTime);
        Map<LocalDate, Integer> orderMap = orderRows.stream()
                .collect(Collectors.toMap(
                        r -> ((java.sql.Date) r.get("day")).toLocalDate(),
                        r -> ((Number) r.get("total")).intValue()));

        List<Map<String, Object>> validRows = reportMapper.countValidOrdersGroupByDay(
                com.nova.entity.Orders.COMPLETED, beginTime, endTime);
        Map<LocalDate, Integer> validMap = validRows.stream()
                .collect(Collectors.toMap(
                        r -> ((java.sql.Date) r.get("day")).toLocalDate(),
                        r -> ((Number) r.get("total")).intValue()));

        List<String> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        fillDateRange(begin, end, orderMap, dateList, orderCountList, 0);

        List<Integer> validOrderCountList = new ArrayList<>();
        fillDateRange(begin, end, validMap, new ArrayList<>(), validOrderCountList, 0);

        Map<String, Object> result = new HashMap<>();
        result.put("dateList", dateList);
        result.put("orderCountList", orderCountList);
        result.put("validOrderCountList", validOrderCountList);
        return Result.success(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出营业数据")
    public void export(HttpServletResponse response, LocalDate begin, LocalDate end) {
        reportService.exportExcel(begin, end, response);
    }
}
