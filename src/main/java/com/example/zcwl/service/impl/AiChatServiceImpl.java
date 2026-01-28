package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Dialog;
import com.example.zcwl.entity.QueAns;
import com.example.zcwl.entity.User;
import com.example.zcwl.repository.DialogRepository;
import com.example.zcwl.repository.QueAnsRepository;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import javax.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI聊天服务实现类
 * 实现核心业务逻辑
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatServiceImpl.class);
    
    private final DialogRepository dialogRepository;
    private final QueAnsRepository queAnsRepository;
    private final UserRepository userRepository;
    private RestTemplate restTemplate;
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    
    @Value("${spring.ai.openai.chat.options.temperature}")
    private Double temperature;

    /**
     * 构造函数
     * @param dialogRepository 对话Repository
     * @param queAnsRepository 问答Repository
     * @param userRepository 用户Repository
     */
    @Autowired
    public AiChatServiceImpl(DialogRepository dialogRepository, QueAnsRepository queAnsRepository, UserRepository userRepository) {
        this.dialogRepository = dialogRepository;
        this.queAnsRepository = queAnsRepository;
        this.userRepository = userRepository;
        this.restTemplate = null; // 将在@PostConstruct中初始化
    }
    
    /**
     * 初始化RestTemplate
     */
    @PostConstruct
    public void init() {
        // 配置RestTemplate
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Transactional
    public Map<String, Object> createNewChat(String uId) {
        // 查找用户
        User user = userRepository.findByuId(uId);
        if (user == null) {
            // 如果用户不存在，抛出异常
            throw new RuntimeException("User not found: " + uId);
        }
        
        // 创建新的对话
        Dialog dialog = new Dialog();
        dialog.setUser(user);
        dialog.setQaTimes(0);
        Dialog savedDialog = dialogRepository.save(dialog);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedDialog.getdId());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> getChatsByUserId(String uId) {
        // 查询用户的所有对话
        List<Dialog> dialogs = dialogRepository.findByUserUId(uId);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> chats = new ArrayList<>();
        
        for (Dialog dialog : dialogs) {
            Map<String, Object> chatInfo = new HashMap<>();
            chatInfo.put("id", dialog.getdId());
            chatInfo.put("u_id", dialog.getUser().getUId());
            chatInfo.put("chat", buildChatMessages(dialog.getdId()));
            chats.add(chatInfo);
        }
        
        result.put("chats", chats);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> getChatById(Integer id) {
        // 查询指定的对话
        Optional<Dialog> optionalDialog = dialogRepository.findById(id);
        if (optionalDialog.isEmpty()) {
            throw new RuntimeException("Dialog not found: " + id);
        }
        
        Dialog dialog = optionalDialog.get();
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", dialog.getdId());
        result.put("u_id", dialog.getUser().getUId());
        result.put("chat", buildChatMessages(dialog.getdId()));
        
        return result;
    }
    
    /**
     * 构建聊天消息列表
     * @param dialogId 对话ID
     * @return 格式化的聊天消息列表
     */
    private List<Map<String, Object>> buildChatMessages(Integer dialogId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 查询该对话的所有问答记录
        List<QueAns> queAnsList = queAnsRepository.findByIdDId(dialogId);
        if (queAnsList != null && !queAnsList.isEmpty()) {
            for (QueAns queAns : queAnsList) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", queAns.getId().getTimes());
                message.put("ask", queAns.getQue());
                message.put("ans", queAns.getAns());
                messages.add(message);
            }
        }
        return messages;
    }

    @Override
    @Transactional
    public Map<String, Object> addChatMessage(Integer id, String ask, String uId) {
        // 查询并验证对话
        Dialog dialog = getValidatedDialog(id, uId);
        
        // 调用AI获取回答（使用block是合理的，因为这是在同步方法中）
        String ans = callAI(ask);
        
        // 保存问答记录并更新对话次数
        int nextTimes = saveChatMessage(dialog, ask, ans);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", nextTimes);
        result.put("ans", ans);
        return result;
    }

    @Override
    @Transactional
    public Flux<String> addChatMessageStream(Integer id, String ask, String uId) {
        // 查询并验证对话
        Dialog dialog = getValidatedDialog(id, uId);
        
        // 创建一个线程安全的StringBuilder来存储完整的回答
        StringBuilder fullAnswer = new StringBuilder();
        
        // 调用AI获取流式回答
        Flux<String> responseFlux = callAIStream(ask);
        
        // 收集完整回答并在结束时保存到数据库
        return responseFlux.publishOn(Schedulers.boundedElastic()).doOnComplete(() -> {
            // 保存问答记录并更新对话次数
                saveChatMessage(dialog, ask, fullAnswer.toString());
        }).doOnNext(fullAnswer::append);
    }
    
    /**
     * 查询并验证对话
     * @param id 对话ID
     * @param uId 用户ID
     * @return 验证后的对话
     */
    private Dialog getValidatedDialog(Integer id, String uId) {
        Optional<Dialog> optionalDialog = dialogRepository.findById(id);
        if (optionalDialog.isEmpty()) {
            throw new RuntimeException("Dialog not found: " + id);
        }
        
        Dialog dialog = optionalDialog.get();
        // 验证用户ID是否匹配
        if (!dialog.getUser().getUId().equals(uId)) {
            throw new RuntimeException("User ID mismatch: expected " + dialog.getUser().getUId() + ", got " + uId);
        }
        
        return dialog;
    }
    
    /**
     * 保存问答记录并更新对话次数
     * @param dialog 对话对象
     * @param ask 问题
     * @param ans 回答
     * @return 下一次问答次数
     */
    private int saveChatMessage(Dialog dialog, String ask, String ans) {
        // 获取当前问答次数
        int qaTimes = dialog.getQaTimes() != null ? dialog.getQaTimes() : 0;
        int nextTimes = qaTimes + 1;
        
        // 创建新的问答记录
        QueAns queAns = new QueAns();
        QueAns.QueAnsId queAnsId = new QueAns.QueAnsId(dialog.getdId(), nextTimes);
        queAns.setId(queAnsId);
        queAns.setDate(LocalDateTime.now());
        queAns.setQue(ask);
        // 保存完整的回答，不进行截断
        queAns.setAns(ans);
        queAns.setDialog(dialog);
        queAnsRepository.save(queAns);
        
        // 更新对话的问答次数
        dialog.setQaTimes(nextTimes);
        dialogRepository.save(dialog);
        
        return nextTimes;
    }
    
    /**
     * 构建AI API请求体
     * @param ask 用户问题
     * @param stream 是否流式响应
     * @return 构建好的请求体
     */
    private Map<String, Object> buildRequestBody(String ask, boolean stream) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个智能助手，需要根据用户的问题提供准确、有用的回答。");
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", ask);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("stream", stream);
        
        return requestBody;
    }

    /**
     * 调用AI模型获取回答
     * @param ask 用户问题
     * @return AI回答
     */
    private String callAI(String ask) {
        logger.info("开始调用AI API，问题: {}", ask);
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(ask, false);
            logger.info("AI API Request Body: {}", requestBody);
            logger.info("AI API Base URL: {}", baseUrl);
            logger.info("AI API Key: {}", apiKey.substring(0, 10) + "...");
            
            // 构建请求URL
            String url = baseUrl + "/chat/completions";
            logger.info("Calling AI API at: {}", url);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            logger.info("AI API Headers: {}", headers);
            
            // 构建请求实体
            RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                    .post(url)
                    .headers(headers)
                    .body(requestBody);
            
            // 调用API
            logger.info("Sending request to AI API...");
            long startTime = System.currentTimeMillis();
            
            // 添加超时设置
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(30000); // 30秒连接超时
            requestFactory.setReadTimeout(60000); // 60秒读取超时
            RestTemplate timeoutRestTemplate = new RestTemplate(requestFactory);
            
            ResponseEntity<Map<String, Object>> responseEntity = timeoutRestTemplate.exchange(
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            
            long endTime = System.currentTimeMillis();
            logger.info("AI API call took {} ms", endTime - startTime);
            
            // 记录响应状态
            logger.info("AI API Response Status: {}", responseEntity.getStatusCode());
            logger.info("AI API Response Headers: {}", responseEntity.getHeaders());
            
            // 获取响应体
            Map<String, Object> response = responseEntity.getBody();
            logger.info("AI API Response Body: {}", response);
            
            // 提取回答
            if (response != null) {
                // 检查是否有错误字段
                if (response.containsKey("error")) {
                    logger.error("AI API returned error: {}", response.get("error"));
                    return "AI API错误: " + response.get("error");
                }
                
                Object choicesObj = response.get("choices");
                if (choicesObj instanceof List<?> choicesList) {
                    logger.info("AI API Choices List Size: {}", choicesList.size());
                    if (!choicesList.isEmpty()) {
                        Object choiceObj = choicesList.get(0);
                        if (choiceObj instanceof Map<?, ?> choiceMap) {
                            Object messageObj = choiceMap.get("message");
                            if (messageObj instanceof Map<?, ?> messageMap) {
                                Object contentObj = messageMap.get("content");
                                if (contentObj instanceof String answer) {
                                    logger.info("AI API Answer: {}", answer.substring(0, Math.min(100, answer.length())) + "...");
                                    return answer;
                                } else {
                                    logger.error("AI API Content is not a string: {}", contentObj);
                                }
                            } else {
                                logger.error("AI API Message is not a map: {}", messageObj);
                            }
                        } else {
                            logger.error("AI API Choice is not a map: {}", choiceObj);
                        }
                    } else {
                        logger.error("AI API Choices list is empty");
                    }
                } else {
                    logger.error("AI API Choices is not a list: {}", choicesObj);
                }
            } else {
                logger.error("AI API Response is null");
            }
        } catch (Exception e) {
            logger.error("Error calling AI API: {}", e.getMessage());
            logger.error("Full exception stack trace:", e);
            // 记录更详细的异常信息
            logger.error("Exception class: {}", e.getClass().getName());
            if (e.getCause() != null) {
                logger.error("Exception cause: {}", e.getCause().getMessage());
            }
        }
        
        logger.info("AI API调用失败，返回默认错误消息");
        return "抱歉，无法获取回答，请稍后再试。";
    }

    /**
     * 调用AI模型获取流式回答
     * @param ask 用户问题
     * @return 流式回答的Flux
     */
    private Flux<String> callAIStream(String ask) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(ask, true);
            
            // 构建请求URL
            String url = baseUrl + "/chat/completions";
            
            // 使用WebClient处理非阻塞的流式响应
            return WebClient.create(url)
                    .post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .map(this::parseStreamResponse)
                    .onErrorResume(e -> {
                        logger.error("Error calling AI stream API: {}", e.getMessage(), e);
                        return Flux.just("抱歉，无法获取回答，请稍后再试。");
                    });
        } catch (Exception e) {
            logger.error("Error calling AI stream API: {}", e.getMessage(), e);
            return Flux.just("抱歉，无法获取回答，请稍后再试。");
        }
    }
    
    /**
     * 解析流式响应
     * @param responseChunk 响应块
     * @return 解析后的内容
     */
    private String parseStreamResponse(String responseChunk) {
        try {
            // 简单解析流式响应
            // 实际项目中可能需要更复杂的解析逻辑
            if (responseChunk.contains("content")) {
                Map<String, Object> parsed = parseJson(responseChunk);
                if (parsed.containsKey("content")) {
                    return parsed.get("content").toString();
                }
            }
            return "";
        } catch (Exception e) {
            logger.error("Error parsing stream response: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 简单的JSON解析方法
     * @param json JSON字符串
     * @return 解析后的Map
     */
    private Map<String, Object> parseJson(String json) {
        // 这里使用简单的实现，实际项目中可以使用Jackson等库
        // 注意：此实现仅用于演示，不支持复杂的JSON结构
        Map<String, Object> result = new HashMap<>();
        try {
            // 移除首尾的大括号
            json = json.trim();
            if (json.startsWith("{")) {
                json = json.substring(1);
            }
            if (json.endsWith("}")) {
                json = json.substring(0, json.length() - 1);
            }
            
            // 分割键值对
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim();
                    
                    // 简单处理字符串值
                    if (value.startsWith("\"")) {
                        value = value.substring(1);
                    }
                    if (value.endsWith("\"")) {
                        value = value.substring(0, value.length() - 1);
                    }
                    
                    result.put(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing JSON: {}", e.getMessage(), e);
        }
        return result;
    }
}
