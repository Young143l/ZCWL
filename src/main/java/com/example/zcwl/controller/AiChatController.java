package com.example.zcwl.controller;

import com.example.zcwl.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * AI聊天控制器
 * 实现REST接口
 */
@RestController
@RequestMapping("/aichatdoc")
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);
    private final AiChatService aiChatService;

    /**
     * 构造函数
     * @param aiChatService AI聊天服务
     */
    @Autowired
    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 查询用户AI聊天记录
     * 接口：GET /aichatdoc/?uId=
     * @param uId 用户ID
     * @return 聊天记录列表
     */
    @GetMapping
    public Map<String, Object> getChatsByUserId(
            @RequestParam("uId") String uId) {
        return aiChatService.getChatsByUserId(uId);
    }

    /**
     * 查询指定ID的AI聊天记录
     * 接口：GET /aichatdoc/{id}
     * @param id 聊天文档ID
     * @return 聊天记录
     */
    @GetMapping("/{id}")
    public Map<String, Object> getChatById(
            @PathVariable Integer id) {
        return aiChatService.getChatById(id);
    }

    /**
     * 创建新的AI对话
     * 接口：POST /aichatdoc/
     * @param request 请求参数
     * @return 包含对话ID的Map
     */
    @PostMapping
    public Map<String, Object> createNewChat(
            @RequestBody Map<String, String> request) {
        String uId = request.get("uId");
        return aiChatService.createNewChat(uId);
    }
    
    /**
     * 创建新的AI对话
     * 接口：POST /aichatdoc/new
     * @param request 请求参数
     * @return 包含对话ID的Map
     */
    @PostMapping("/new")
    public Map<String, Object> createNewChatNew(
            @RequestBody Map<String, String> request) {
        String uId = request.get("uId");
        return aiChatService.createNewChat(uId);
    }

    /**
     * 新增指定ID的AI对话询问（流式返回）
     * 接口：POST /aichatdoc/{id}
     * @param id 聊天文档ID
     * @param request 请求参数
     * @return 流式回答的Flux
     */
    @PostMapping("/{id}")
    public Flux<String> addChatMessage(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        logger.debug("===== 开始处理流式请求 ======");
        logger.debug("对话ID: {}", id);
        logger.debug("请求参数: {}", request);
        
        // 获取请求参数
        String ask = request.get("ask");
        String uId = request.get("uId");
        
        logger.debug("问题: {}", ask);
        logger.debug("用户ID: {}", uId);
        
        // 验证参数
        if (ask == null || ask.isEmpty()) {
            logger.error("请求参数错误: ask参数为空");
            return Flux.error(new IllegalArgumentException("请求参数错误: ask参数不能为空"));
        }
        
        if (uId == null || uId.isEmpty()) {
            logger.error("请求参数错误: uId参数为空");
            return Flux.error(new IllegalArgumentException("请求参数错误: uId参数不能为空"));
        }
        
        logger.debug("调用aiChatService.addChatMessageStream获取流式回答...");
        
        // 调用AI获取流式回答并直接返回Flux
        // Spring WebFlux会自动处理流式响应的生命周期
        return aiChatService.addChatMessageStream(id, ask, uId)
                .doOnNext(content -> {
                    logger.debug("发送流式数据，长度: {}", content.length());
                })
                .doOnError(error -> {
                    logger.error("获取流式回答失败: {}", error.getMessage());
                })
                .doOnComplete(() -> {
                    logger.debug("流式响应完成");
                });
    }
    
    /**
     * 全局异常处理
     * @param e 异常对象
     * @param response HTTP响应对象
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletResponse response) {
        logger.error("全局异常捕获: {}", e.getMessage(), e);
        try {
            // 检查是否是Dialog not found错误
            if (e.getMessage() != null && e.getMessage().startsWith("Dialog not found")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("对话不存在: " + e.getMessage());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("服务器内部错误: " + e.getMessage());
            }
            response.getWriter().close();
        } catch (Exception ex) {
            logger.error("发送错误响应失败: {}", ex.getMessage(), ex);
        }
    }
}