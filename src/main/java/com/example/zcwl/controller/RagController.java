package com.example.zcwl.controller;

import com.example.zcwl.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * RAG控制器
 * 处理基于向量检索的问答服务请求
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger logger = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;

    /**
     * 构造函数
     * @param ragService RAG服务
     */
    @Autowired
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * RAG问答接口
     * @param request 包含问题的请求体
     * @return 包含回答的响应
     */
    @PostMapping("/answer")
    public Map<String, Object> ragAnswer(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("answer", "请提供有效的问题");
            errorResponse.put("error", "Question is required");
            return errorResponse;
        }

        // 对用户问题做脱敏处理，只记录前50个字符
        String maskedQuestion = question.length() > 50 ? question.substring(0, 50) + "..." : question;
        logger.info("RAG问答请求，问题: {}", maskedQuestion);

        try {
            return ragService.ragAnswer(question);
        } catch (Exception e) {
            logger.error("RAG问答接口异常: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("answer", "抱歉，无法获取回答，请稍后再试。");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * RAG流式问答接口
     * @param request 包含问题的请求体
     * @return 流式回答的响应
     */
    @PostMapping(value = "/answer/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> ragAnswerStream(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Flux.just("请提供有效的问题");
        }

        // 对用户问题做脱敏处理，只记录前50个字符
        String maskedQuestion = question.length() > 50 ? question.substring(0, 50) + "..." : question;
        logger.info("RAG流式问答请求，问题: {}", maskedQuestion);

        try {
            return ragService.ragAnswerStream(question)
                    .doOnNext(content -> {
                        logger.debug("发送流式数据，长度: {}", content.length());
                    })
                    .doOnError(error -> {
                        logger.error("获取流式回答失败: {}", error.getMessage());
                    })
                    .doOnComplete(() -> {
                        logger.debug("流式响应完成");
                    });
        } catch (Exception e) {
            logger.error("RAG流式问答接口异常: {}", e.getMessage(), e);
            return Flux.just("抱歉，无法获取回答，请稍后再试。");
        }
    }

    /**
     * 向量搜索API
     * @param request 包含查询的请求体
     * @return 搜索结果列表
     */
    @PostMapping("/search")
    public List<Map<String, Object>> searchSimilar(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        logger.info("向量搜索请求，查询: {}", query.substring(0, Math.min(50, query.length())) + "...");

        try {
            // 调用ragService的方法进行向量搜索
            // 注意：这里需要在RagService中添加对应的方法
            float[] embedding = ragService.embedText(query);
            return ragService.searchSimilar(embedding, 10);
        } catch (Exception e) {
            logger.error("向量搜索接口异常: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
}
