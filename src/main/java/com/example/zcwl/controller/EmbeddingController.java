package com.example.zcwl.controller;

import com.example.zcwl.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 嵌入API控制器
 * 处理文本嵌入请求
 */
@RestController
@RequestMapping("/api/ai")
public class EmbeddingController {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingController.class);

    private final RagService ragService;

    /**
     * 构造函数
     * @param ragService RAG服务
     */
    @Autowired
    public EmbeddingController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 文本嵌入API
     * @param request 包含文本的请求体
     * @return 嵌入向量
     */
    @PostMapping("/embedding")
    public Map<String, Object> embedText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.trim().isEmpty()) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Text is required");
            return errorResponse;
        }

        logger.info("文本嵌入请求，文本长度: {}", text.length());

        try {
            float[] embedding = ragService.embedText(text);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("embedding", embedding);
            return response;
        } catch (Exception e) {
            logger.error("文本嵌入接口异常: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}
