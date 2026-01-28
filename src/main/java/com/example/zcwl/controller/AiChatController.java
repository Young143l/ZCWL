package com.example.zcwl.controller;

import com.example.zcwl.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI聊天控制器
 * 实现REST接口
 */
@RestController
@RequestMapping("/aichatdoc")
public class AiChatController {

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
     * 接口：GET /aichatdoc/?u_id=
     * @param uId 用户ID
     * @return 聊天记录列表
     */
    @GetMapping
    public Map<String, Object> getChatsByUserId(
            @RequestParam("u_id") String uId) {
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
        String uId = request.get("u_id");
        return aiChatService.createNewChat(uId);
    }

    /**
     * 新增指定ID的AI对话询问
     * 接口：POST /aichatdoc/{id}
     * @param id 聊天文档ID
     * @param request 请求参数
     * @return 包含回答的Map
     */
    @PostMapping("/{id}")
    public Map<String, Object> addChatMessage(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        String ask = request.get("ask");
        String uId = request.get("u_id");
        return aiChatService.addChatMessage(id, ask, uId);
    }
}
