package com.nova.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nova MCP Server 启动类。
 * <p>
 * M0 阶段目标：验证 MCP Java SDK 与 Spring Boot 3.1.12 的兼容性，
 * 仅提供一个 echo 工具用于连通性验证。
 */
@SpringBootApplication
public class McpApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}
