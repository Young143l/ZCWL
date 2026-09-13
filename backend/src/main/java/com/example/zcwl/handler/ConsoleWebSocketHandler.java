package com.example.zcwl.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.zcwl.service.ConsoleService;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制台WebSocket处理器
 * 处理WebSocket连接，接收和发送消息
 */
public class ConsoleWebSocketHandler extends TextWebSocketHandler {

    // 存储会话和对应的控制台服务实例
    private static final Map<WebSocketSession, ConsoleService> sessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 新连接建立时，创建控制台服务实例
        ConsoleService consoleService = new ConsoleService(session);
        sessionMap.put(session, consoleService);
        // 启动控制台服务
        consoleService.start();
        // 发送连接成功消息
        session.sendMessage(new TextMessage("Console connected!\n"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端发送的消息
        ConsoleService consoleService = sessionMap.get(session);
        if (consoleService != null) {
            // 将消息发送给控制台服务
            consoleService.sendInput(message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭时，停止控制台服务
        ConsoleService consoleService = sessionMap.remove(session);
        if (consoleService != null) {
            consoleService.stop();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // 处理传输错误
        System.err.println("WebSocket error: " + exception.getMessage());
        // 移除会话
        ConsoleService consoleService = sessionMap.remove(session);
        if (consoleService != null) {
            consoleService.stop();
        }
    }
}
