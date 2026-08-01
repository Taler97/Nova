package com.nova.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    void exportExcel(LocalDate begin, LocalDate end, HttpServletResponse response);
}
