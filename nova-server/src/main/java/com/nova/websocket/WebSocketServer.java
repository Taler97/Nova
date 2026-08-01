package com.nova.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/ws/admin/notification")
@Slf4j
public class WebSocketServer {

    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        log.info("WebSocket 连接建立，当前连接数：{}", sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        log.info("WebSocket 连接关闭，当前连接数：{}", sessions.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到消息：{}", message);
    }

    public void sendToAll(String message) {
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                log.error("WebSocket 发送失败", e);
            }
        }
    }
}
