package com.nova.service.impl;

import com.nova.entity.Orders;
import com.nova.mapper.ReportMapper;
import com.nova.service.ReportService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    public void exportExcel(LocalDate begin, LocalDate end, HttpServletResponse response) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("营业数据");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("日期");
            header.createCell(1).setCellValue("营业额");
            header.createCell(2).setCellValue("订单数");
            header.createCell(3).setCellValue("有效订单数");

            List<Orders> orders = reportMapper.getOrdersByTime(beginTime, endTime);

            // 按日期分组，O(1) 查找替代 O(n) 遍历
            Map<LocalDate, List<Orders>> ordersByDate = orders.stream()
                    .filter(o -> o.getOrderTime() != null)
                    .collect(Collectors.groupingBy(o -> o.getOrderTime().toLocalDate()));

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                if (date.isAfter(end)) break;

                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(date.toString());

                BigDecimal turnover = BigDecimal.ZERO;
                int totalOrders = 0;
                int validOrders = 0;

                List<Orders> dayOrders = ordersByDate.getOrDefault(date, Collections.emptyList());
                for (Orders o : dayOrders) {
                    totalOrders++;
                    if (o.getStatus().equals(Orders.COMPLETED)) {
                        validOrders++;
                        turnover = turnover.add(o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO);
                    }
                }

                row.createCell(1).setCellValue(turnover.doubleValue());
                row.createCell(2).setCellValue(totalOrders);
                row.createCell(3).setCellValue(validOrders);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=report.xlsx");

            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
    }
}
