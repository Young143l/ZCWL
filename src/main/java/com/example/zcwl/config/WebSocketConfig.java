package com.example.zcwl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.example.zcwl.handler.ConsoleWebSocketHandler;

/**
 * WebSocket配置类
 * 用于注册WebSocket处理器和配置WebSocket路径
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册控制台WebSocket处理器，路径为/console
        // 允许跨域请求
        registry.addHandler(new ConsoleWebSocketHandler(), "/console")
                .setAllowedOrigins("*");
    }
}
