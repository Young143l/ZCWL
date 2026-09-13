package com.example.zcwl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.example.zcwl.handler.ConsoleWebSocketHandler;
import com.example.zcwl.handler.CodeWebSocketHandler;

/**
 * WebSocket配置类
 * 用于注册WebSocket处理器和配置WebSocket路径
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CodeWebSocketHandler codeWebSocketHandler;
    
    /**
     * 构造函数
     * @param codeWebSocketHandler 代码WebSocket处理器
     */
    @Autowired
    public WebSocketConfig(CodeWebSocketHandler codeWebSocketHandler) {
        this.codeWebSocketHandler = codeWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册控制台WebSocket处理器，路径为/console
        // 允许跨域请求
        registry.addHandler(new ConsoleWebSocketHandler(), "/console")
                .setAllowedOrigins("*");
        
        // 注册代码运行WebSocket处理器，路径为/ws/code/cp/{id}
        // 允许跨域请求
        registry.addHandler(codeWebSocketHandler, "/ws/code/cp/*")
                .setAllowedOrigins("*");
    }
}
