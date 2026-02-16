package com.example.zcwl.service.impl;

import com.example.zcwl.entity.DocRag;
import com.example.zcwl.repository.DocRagRepository;
import com.example.zcwl.service.AiChatService;
import com.example.zcwl.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务实现类
 * 实现基于向量检索的问答服务核心逻辑
 */
@Service
public class RagServiceImpl implements RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagServiceImpl.class);

    private final RestTemplate restTemplate;
    private final DocRagRepository docRagRepository;
    private final AiChatService aiChatService;

    // 向量化模型配置
    @Value("${rag.embedding.api-key}")
    private String embeddingApiKey;

    @Value("${rag.embedding.base-url}")
    private String embeddingBaseUrl;

    @Value("${rag.embedding.model}")
    private String embeddingModel;

    // 向量检索配置
    @Value("${rag.vector.search.limit}")
    private int searchLimit;

    /**
     * 构造函数
     * @param restTemplate RestTemplate实例
     * @param docRagRepository DocRagRepository实例
     * @param aiChatService AI聊天服务
     */
    @Autowired
    public RagServiceImpl(RestTemplate restTemplate, DocRagRepository docRagRepository, AiChatService aiChatService) {
        this.restTemplate = restTemplate;
        this.docRagRepository = docRagRepository;
        this.aiChatService = aiChatService;
    }

    @Override
    public Map<String, Object> ragAnswer(String question) {
        try {
            // 1. 将用户问题向量化
            float[] embedding = embedText(question);

            // 2. 检索相似文本片段
            List<Map<String, Object>> similarTexts = searchSimilar(embedding, searchLimit);

            // 3. 组合上下文
            String context = buildContext(similarTexts, question);

            // 4. 调用AI模型获取回答
            // 使用临时对话ID 0，因为RAG问答不需要上下文历史
            String answer = aiChatService.callAI(0, context);

            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("answer", answer);
            result.put("sources", similarTexts);
            return result;
        } catch (Exception e) {
            logger.error("RAG问答失败: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("answer", "抱歉，无法获取回答，请稍后再试。");
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Flux<String> ragAnswerStream(String question) {
        try {
            // 1. 将用户问题向量化
            float[] embedding = embedText(question);

            // 2. 检索相似文本片段
            List<Map<String, Object>> similarTexts = searchSimilar(embedding, searchLimit);

            // 3. 组合上下文
            String context = buildContext(similarTexts, question);

            // 4. 调用AI模型获取流式回答
            // 使用临时对话ID 0，因为RAG问答不需要上下文历史
            return aiChatService.callAIStream(0, context);
        } catch (Exception e) {
            logger.error("RAG流式问答失败: {}", e.getMessage(), e);
            return Flux.just("抱歉，无法获取回答，请稍后再试。");
        }
    }

    @Override
    @Retryable(
            retryFor = {Exception.class},
//            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public float[] embedText(String text) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("input", text);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + embeddingApiKey);

            // 构建请求实体
            RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                    .post(URI.create(embeddingBaseUrl + "/embeddings"))
                    .headers(headers)
                    .body(requestBody);

            // 调用API
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            // 解析响应
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("data")) {
                List<?> dataList = (List<?>) response.get("data");
                if (!dataList.isEmpty()) {
                    Map<?, ?> dataItem = (Map<?, ?>) dataList.getFirst();
                    if (dataItem.containsKey("embedding")) {
                        Object embeddingObj = dataItem.get("embedding");
                        if (embeddingObj instanceof List<?> rawEmbeddingList) {
                            float[] embedding = new float[rawEmbeddingList.size()];
                            for (int i = 0; i < rawEmbeddingList.size(); i++) {
                                Object item = rawEmbeddingList.get(i);
                                if (item instanceof Double) {
                                    embedding[i] = ((Double) item).floatValue();
                                } else if (item instanceof Number) {
                                    embedding[i] = ((Number) item).floatValue();
                                }
                            }
                            return embedding;
                        }
                    }
                }
            }
            throw new RuntimeException("Failed to get embedding");
        } catch (Exception e) {
            logger.error("文本向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("文本向量化失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> searchSimilar(float[] embedding, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();

        logger.info("开始向量检索，limit: {}", limit);

        // 构建向量字符串（实际向量格式使用逗号分隔）
        StringBuilder vectorBuilder = new StringBuilder();
        vectorBuilder.append('[');
        for (int i = 0; i < embedding.length; i++) {
            vectorBuilder.append(embedding[i]);
            if (i < embedding.length - 1) {
                vectorBuilder.append(',');
            }
        }
        vectorBuilder.append(']');
        String vectorString = vectorBuilder.toString();

        logger.info("向量字符串长度: {}", vectorString.length());
        logger.info("向量前100字符: {}", vectorString.substring(0, Math.min(100, vectorString.length())) + "...");

        try {
            // 使用DocRagRepository进行向量搜索
            results = docRagRepository.searchSimilar(vectorString, limit);
            logger.info("向量检索结果数量: {}", results.size());

            // 处理结果，确保返回格式一致
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (Map<String, Object> result : results) {
                Map<String, Object> formattedResult = new HashMap<>();
                formattedResult.put("id", result.get("id"));
                formattedResult.put("content", result.get("chunk"));
                formattedResult.put("distance", result.get("distance"));
                formattedResults.add(formattedResult);
            }
            results = formattedResults;

        } catch (Exception e) {
            logger.error("向量检索失败: {}", e.getMessage(), e);
            // 返回空列表，避免因数据库错误导致整个流程失败
        }

        // 如果没有找到相关文档，检查doc_rag表是否存在数据
        if (results.isEmpty()) {
            logger.info("未找到相关文档，检查doc_rag表数据情况");
            try {
                // 检查表中的数据量
                long totalCount = docRagRepository.count();
                logger.info("doc_rag表中的数据量: {}", totalCount);
                
                if (totalCount > 0) {
                    // 检查前5条数据，确认chunk和vector列是否有值
                    List<DocRag> sampleDocs = docRagRepository.findTop5ByIdNotNull();
                    logger.info("doc_rag表前5条数据:");
                    for (DocRag doc : sampleDocs) {
                        logger.info("  ID: {}, Chunk前50字符: {}, Vector是否为空: {}", 
                                doc.getId(), 
                                doc.getChunk() != null ? doc.getChunk().substring(0, Math.min(50, doc.getChunk().length())) + "..." : "空",
                                doc.getVector() == null);
                    }
                } else {
                    logger.info("doc_rag表为空");
                }
            } catch (Exception e) {
                logger.error("检查doc_rag表失败: {}", e.getMessage(), e);
            }
        }

        logger.info("向量检索完成，返回结果数量: {}", results.size());
        return results;
    }

    @Override
    public String buildContext(List<Map<String, Object>> similarTexts, String question) {
        StringBuilder contextBuilder = new StringBuilder();

        // 添加指令告诉AI
        contextBuilder.append("请回答以下问题。直接提供清晰、准确的答案，不要提及任何关于参考资料的内容，不要使用'根据资料'、'参考'等类似表述。\n\n");

        // 添加检索到的文本片段，作为背景信息
        for (Map<String, Object> text : similarTexts) {
            contextBuilder.append(text.get("content"));
            contextBuilder.append("\n\n");
        }

        // 添加用户问题
        contextBuilder.append("问题：\n");
        contextBuilder.append(question);

        return contextBuilder.toString();
    }


}
