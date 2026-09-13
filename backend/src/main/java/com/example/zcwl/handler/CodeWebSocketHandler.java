package com.example.zcwl.handler;

import com.example.zcwl.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 代码运行WebSocket处理器
 * 处理WebSocket连接，接收和发送消息，运行代码
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodeWebSocketHandler extends TextWebSocketHandler {

    // 存储会话和对应的进程信息
    private static final Map<WebSocketSession, ProcessInfo> sessionMap = new HashMap<>();
    // JWT token工具
    private JwtTokenUtil jwtTokenUtil;
    // JSON解析器
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构造函数
     * @param jwtTokenUtil JWT token工具
     */
    @Autowired
    public CodeWebSocketHandler(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取token参数
        String token = getTokenFromSession(session);
        // 验证token
        if (!validateToken(token)) {
            // token无效，关闭连接
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // 获取id参数
        String id = getIdFromSession(session);

        // 创建进程信息对象
        ProcessInfo processInfo = new ProcessInfo();
        sessionMap.put(session, processInfo);

        // 发送连接成功消息
        sendMessage(session, false, "Connection established");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 解析JSON消息
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            boolean done = jsonNode.get("done").asBoolean();

            if (done) {
                // 前端发送结束消息，停止进程
                stopProcess(session);
                sendMessage(session, true, "User stopped execution");
            } else {
                // 检查是否是代码执行请求还是输入请求
                String input = jsonNode.get("input").asText();
                ProcessInfo processInfo = sessionMap.get(session);
                
                // 如果已经有运行的进程，说明这是输入数据
                if (processInfo != null && processInfo.getProcess() != null && !processInfo.getProcess().isAlive()) {
                    // 进程已结束，重新运行代码
                    runCode(session, input);
                } else if (processInfo != null && processInfo.getInputWriter() != null) {
                    // 进程正在运行，发送输入到进程
                    processInfo.getInputWriter().println(input);
                    processInfo.getInputWriter().flush();
                } else {
                    // 首次运行代码
                    runCode(session, input);
                }
            }
        } catch (Exception e) {
            try {
                sendMessage(session, true, "Error processing message: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭时，停止进程
        stopProcess(session);
        sessionMap.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // 处理传输错误
        System.err.println("WebSocket error: " + exception.getMessage());
        // 停止进程
        stopProcess(session);
        sessionMap.remove(session);
    }

    /**
     * 从会话中获取token参数
     * @param session WebSocket会话
     * @return token
     */
    private String getTokenFromSession(WebSocketSession session) {
        Map<String, String> params = getQueryParams(session.getUri().getQuery());
        return params.get("token");
    }

    /**
     * 从会话中获取id参数
     * @param session WebSocket会话
     * @return id
     */
    private String getIdFromSession(WebSocketSession session) {
        // 从路径中获取id
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length >= 5) {
            return parts[4];
        }
        return "unknown";
    }

    /**
     * 解析查询参数
     * @param query 查询字符串
     * @return 参数映射
     */
    private Map<String, String> getQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1].replace("\"", ""));
                }
            }
        }
        return params;
    }

    /**
     * 验证token
     * @param token JWT token
     * @return 是否有效
     */
    private boolean validateToken(String token) {
        // 验证JWT token
        return jwtTokenUtil.validateToken(token);
    }

    /**
     * 运行代码
     * @param session WebSocket会话
     * @param code 代码
     */
    private void runCode(WebSocketSession session, String code) {
        ProcessInfo processInfo = sessionMap.get(session);
        if (processInfo == null) {
            return;
        }

        // 停止之前的进程
        stopProcess(session);

        try {
            // 创建临时文件
            File tempFile = File.createTempFile("code", ".py");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                writer.write(code);
            }

            // 启动Python进程，启用标准输入
            ProcessBuilder processBuilder = new ProcessBuilder("python", tempFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            // 设置PYTHONIOENCODING环境变量，确保Python进程的输出编码是UTF-8
            Map<String, String> env = processBuilder.environment();
            env.put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();
            
            // 获取进程的输入流，用于向Python程序发送输入
            PrintWriter inputWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);

            // 更新进程信息
            processInfo.setProcess(process);
            processInfo.setTempFile(tempFile);
            processInfo.setInputWriter(inputWriter);

            // 启动线程读取进程输出
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            processInfo.setExecutorService(executorService);

            executorService.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 发送输出到WebSocket
                        sendMessage(session, false, line);
                    }
                } catch (Exception e) {
                    // 连接已关闭，忽略异常
                }

                // 进程结束时发送结束消息
                try {
                    int exitCode = process.waitFor();
                    sendMessage(session, true, "Process exited with code: " + exitCode);
                } catch (Exception e) {
                    // 连接已关闭，忽略异常
                }
            });

        } catch (Exception e) {
            try {
                sendMessage(session, true, "Error running code: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 停止进程
     * @param session WebSocket会话
     */
    private void stopProcess(WebSocketSession session) {
        ProcessInfo processInfo = sessionMap.get(session);
        if (processInfo != null) {
            // 关闭输入流
            if (processInfo.getInputWriter() != null) {
                processInfo.getInputWriter().close();
            }
            // 销毁进程
            if (processInfo.getProcess() != null) {
                processInfo.getProcess().destroyForcibly();
            }
            // 关闭线程池
            if (processInfo.getExecutorService() != null) {
                processInfo.getExecutorService().shutdownNow();
            }
            // 删除临时文件
            if (processInfo.getTempFile() != null) {
                processInfo.getTempFile().delete();
            }
            // 清空输入写入器引用
            processInfo.setInputWriter(null);
        }
    }

    /**
     * 发送消息
     * @param session WebSocket会话
     * @param done 是否结束
     * @param message 消息内容
     * @throws IOException 异常
     */
    private void sendMessage(WebSocketSession session, boolean done, String message) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("done", done);
        response.put("message", message);
        String json = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(json.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 进程信息类
     */
    private static class ProcessInfo {
        private Process process;
        private File tempFile;
        private ExecutorService executorService;
        private PrintWriter inputWriter;  // 用于向进程发送输入

        public Process getProcess() {
            return process;
        }

        public void setProcess(Process process) {
            this.process = process;
        }

        public File getTempFile() {
            return tempFile;
        }

        public void setTempFile(File tempFile) {
            this.tempFile = tempFile;
        }

        public ExecutorService getExecutorService() {
            return executorService;
        }

        public void setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
        }

        public PrintWriter getInputWriter() {
            return inputWriter;
        }

        public void setInputWriter(PrintWriter inputWriter) {
            this.inputWriter = inputWriter;
        }
    }
}