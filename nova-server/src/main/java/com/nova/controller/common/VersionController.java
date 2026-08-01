package com.nova.controller.common;

import com.nova.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "公共接口-版本信息")
public class VersionController {

    @Value("${nova.info.name}")
    private String appName;

    @Value("${nova.info.version}")
    private String appVersion;

    @Value("${nova.info.build-time}")
    private String buildTime;

    @Value("${nova.info.java-version}")
    private String javaVersion;

    @Value("${nova.info.spring-boot-version}")
    private String springBootVersion;

    @GetMapping("/version")
    @Operation(summary = "查询系统版本信息")
    public Result<Map<String, String>> getVersion() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("name", appName);
        info.put("version", appVersion);
        info.put("buildTime", buildTime);
        info.put("javaVersion", javaVersion);
        info.put("springBootVersion", springBootVersion);
        return Result.success(info);
    }
}
