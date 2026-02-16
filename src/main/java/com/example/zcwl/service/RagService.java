package com.example.zcwl.service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * RAG服务接口
 * 定义基于向量检索的问答服务核心方法
 */
public interface RagService {

    /**
     * 基于RAG的问答方法
     * @param question 用户问题
     * @return 包含回答的Map
     */
    Map<String, Object> ragAnswer(String question);

    /**
     * 基于RAG的流式问答方法
     * @param question 用户问题
     * @return 流式回答的Flux
     */
    Flux<String> ragAnswerStream(String question);

    /**
     * 将文本向量化
     * @param text 文本内容
     * @return 向量数组
     */
    float[] embedText(String text);

    /**
     * 检索相似文本片段
     * @param embedding 向量
     * @param limit 限制数量
     * @return 相似文本片段列表
     */
    List<Map<String, Object>> searchSimilar(float[] embedding, int limit);

    /**
     * 组合上下文
     * @param similarTexts 相似文本片段列表
     * @param question 用户问题
     * @return 组合后的上下文
     */
    String buildContext(List<Map<String, Object>> similarTexts, String question);
}
