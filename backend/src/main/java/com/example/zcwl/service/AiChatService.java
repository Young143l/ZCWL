package com.example.zcwl.service;

import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI聊天服务接口
 * 定义核心业务逻辑
 */
public interface AiChatService {

    /**
     * 创建新的AI对话
     * @param uId 用户ID
     * @return 包含对话ID的Map
     */
    Map<String, Object> createNewChat(String uId);

    /**
     * 根据用户ID查询聊天文档列表
     * @param uId 用户ID
     * @return 聊天文档列表
     */
    Map<String, Object> getChatsByUserId(String uId);

    /**
     * 根据ID查询指定的聊天文档
     * @param id 聊天文档ID
     * @return 聊天文档
     */
    Map<String, Object> getChatById(Integer id);

    /**
     * 新增指定ID的AI对话询问
     * @param id 聊天文档ID
     * @param ask 用户问题
     * @param uId 用户ID
     * @param img 图片URL（可为空）
     * @return 包含回答的Map
     */
    Map<String, Object> addChatMessage(Integer id, String ask, String uId, String img);

    /**
     * 新增指定ID的AI对话询问（流式返回）
     * @param id 聊天文档ID
     * @param ask 用户问题
     * @param uId 用户ID
     * @param img 图片URL（可为空）
     * @return 流式返回的回答
     */
    Flux<String> addChatMessageStream(Integer id, String ask, String uId, String img);

    /**
     * 调用AI模型获取回答
     * @param dialogId 对话ID
     * @param ask 用户问题
     * @param img 图片URL（可为空）
     * @return AI回答
     */
    String callAI(Integer dialogId, String ask, String img);

    /**
     * 调用AI模型获取流式回答
     * @param dialogId 对话ID
     * @param ask 用户问题
     * @param img 图片URL（可为空）
     * @return 流式回答的Flux
     */
    Flux<String> callAIStream(Integer dialogId, String ask, String img);
}
