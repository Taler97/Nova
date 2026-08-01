package com.nova.controller.admin;

import com.nova.result.Result;
import com.nova.service.WorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/admin/workbench")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端-工作台接口")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @GetMapping("/todayData")
    @Operation(summary = "今日数据")
    public Result<Map<String, Object>> todayData() {
        return Result.success(workbenchService.getTodayData());
    }
}
