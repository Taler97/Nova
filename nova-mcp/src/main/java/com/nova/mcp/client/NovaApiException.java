package com.nova.mcp.client;

/**
 * 调用 nova-server 失败时抛出，携带可直接展示给 LLM 的可读消息。
 */
public class NovaApiException extends RuntimeException {

    public NovaApiException(String message) {
        super(message);
    }

    public NovaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
