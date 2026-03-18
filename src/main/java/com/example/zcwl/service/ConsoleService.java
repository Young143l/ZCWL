package com.example.zcwl.service;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 控制台服务类
 * 使用ProcessBuilder运行外部命令，处理输入输出
 */
public class ConsoleService {

    private WebSocketSession session;
    private Process process;
    private PrintWriter outputWriter;
    private ExecutorService executorService;

    /**
     * 构造函数
     * @param session WebSocket会话
     */
    public ConsoleService(WebSocketSession session) {
        this.session = session;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * 启动控制台服务
     */
    public void start() {
        try {
            // 启动命令行进程
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            // 根据操作系统选择命令
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd.exe");
            } else {
                processBuilder.command("bash");
            }
            
            // 重定向输入输出
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            // 获取输入输出流
            outputWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);
            InputStream inputStream = process.getInputStream();

            // 启动线程读取进程输出
            executorService.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 发送输出到WebSocket
                        session.sendMessage(new TextMessage(line + "\n"));
                    }
                } catch (Exception e) {
                    // 连接已关闭，忽略异常
                }
            });

            // 启动线程监控进程状态
            executorService.submit(() -> {
                try {
                    int exitCode = process.waitFor();
                    // 进程结束时发送退出信息
                    session.sendMessage(new TextMessage("Process exited with code: " + exitCode + "\n"));
                } catch (Exception e) {
                    // 连接已关闭，忽略异常
                }
            });

        } catch (Exception e) {
            try {
                session.sendMessage(new TextMessage("Error starting console: " + e.getMessage() + "\n"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 发送输入到进程
     * @param input 输入内容
     */
    public void sendInput(String input) {
        if (outputWriter != null) {
            outputWriter.println(input);
        }
    }

    /**
     * 停止控制台服务
     */
    public void stop() {
        if (process != null) {
            process.destroyForcibly(); // 强制销毁进程
        }
        if (executorService != null) {
            executorService.shutdownNow(); // 强制终止所有线程
        }
    }
}
