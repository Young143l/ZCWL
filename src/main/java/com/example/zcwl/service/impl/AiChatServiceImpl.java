package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Dialog;
import com.example.zcwl.entity.QueAns;
import com.example.zcwl.entity.User;
import com.example.zcwl.repository.DialogRepository;
import com.example.zcwl.repository.QueAnsRepository;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.service.AiChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

// Resilience4j 导入
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    // 使用Resilience4j的RateLimiter进行限流，替代Guava的不稳定实现
    private final RateLimiter rateLimiter;
    
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
     * @param redisTemplate Redis模板
     * @param restTemplate 全局 RestTemplate Bean
     */
    @Autowired
    public AiChatServiceImpl(
            DialogRepository dialogRepository, 
            QueAnsRepository queAnsRepository, 
            UserRepository userRepository, 
            StringRedisTemplate redisTemplate,
            RestTemplate restTemplate
    ) {
        this.dialogRepository = dialogRepository;
        this.queAnsRepository = queAnsRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate; // 使用全局 RestTemplate
        this.objectMapper = new ObjectMapper(); // 初始化Jackson ObjectMapper
        
        // 使用Resilience4j创建RateLimiter，每秒最多5个请求
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5) // 每秒允许的请求数
                .limitRefreshPeriod(java.time.Duration.ofSeconds(1)) // 刷新周期
                .timeoutDuration(java.time.Duration.ofMillis(0)) // 超时时间
                .build();
        
        // 创建RateLimiter实例
        this.rateLimiter = RateLimiter.of("aiChatRateLimiter", config);
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
        // 构建缓存键
        String cacheKey = "chat:" + id;
        
        try {
            // 尝试从缓存中获取
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                logger.info("从缓存中获取对话数据: {}", id);
                // 反序列化缓存数据，添加类型参数避免未检查赋值
                return objectMapper.readValue(cachedData, new com.fasterxml.jackson.core.type.TypeReference<>() {
                });
            }
        } catch (Exception e) {
            logger.error("Redis缓存读取错误: {}", e.getMessage(), e);
            // 缓存错误不影响正常流程，继续从数据库查询
        }
        
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
        
        try {
            // 将结果存入缓存，设置过期时间为1小时
            redisTemplate.opsForValue().set(
                cacheKey,
                objectMapper.writeValueAsString(result),
                Duration.ofHours(1)
            );
            logger.info("对话数据已存入缓存: {}", id);
        } catch (Exception e) {
            logger.error("Redis缓存写入错误: {}", e.getMessage(), e);
            // 缓存错误不影响正常流程
        }
        
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
        // 对用户问题做脱敏处理，只记录前50个字符
        String maskedAsk = ask.length() > 50 ? ask.substring(0, 50) + "..." : ask;
        logger.info("添加聊天消息，对话ID: {}, 用户ID: {}, 问题: {}", id, uId, maskedAsk);
        
        // 查询并验证对话
        Dialog dialog = getValidatedDialog(id, uId);
        
        // 调用AI获取回答（使用block是合理的，因为这是在同步方法中）
        String ans = callAI(id, ask);
        
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
        // 对用户问题做脱敏处理，只记录前50个字符
        String maskedAsk = ask.length() > 50 ? ask.substring(0, 50) + "..." : ask;
        logger.info("添加流式聊天消息，对话ID: {}, 用户ID: {}, 问题: {}", id, uId, maskedAsk);
        
        // 查询并验证对话
        Dialog dialog = getValidatedDialog(id, uId);
        
        // 创建一个线程安全的StringBuilder来存储完整的回答
        StringBuilder fullAnswer = new StringBuilder();
        
        // 调用AI获取流式回答
        Flux<String> responseFlux = callAIStream(id, ask);
        
        // 收集完整回答并在结束时保存到数据库
        // 注意：不要使用publishOn，否则会导致流被缓冲
        return responseFlux
            .doOnNext(chunk -> {
                // 每次收到数据块时，添加到完整回答中
                fullAnswer.append(chunk);
                logger.debug("收到AI回答数据块，长度: {}", chunk.length());
            })
            .doOnComplete(() -> {
                // 流结束时保存完整回答到数据库
                // 使用单独的线程池执行阻塞的数据库操作，避免在非阻塞上下文中阻塞线程
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        logger.info("流式回答结束，保存完整回答到数据库，长度: {}", fullAnswer.length());
                        saveChatMessage(dialog, ask, fullAnswer.toString());
                    } catch (Exception e) {
                        logger.error("保存对话消息失败: {}", e.getMessage(), e);
                    }
                }, java.util.concurrent.Executors.newSingleThreadExecutor());
            });
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
        
        // 清除缓存，保证数据一致性
        try {
            String cacheKey = "chat:" + dialog.getdId();
            redisTemplate.delete(cacheKey);
            logger.info("清除对话缓存: {}", dialog.getdId());
        } catch (Exception e) {
            logger.error("Redis缓存删除错误: {}", e.getMessage(), e);
            // 缓存错误不影响正常流程
        }
        
        return nextTimes;
    }
    
    /**
     * 构建AI API请求体
     * 支持上下文对话，添加历史问答记录
     * @param dialogId 对话ID
     * @param ask 用户问题
     * @param stream 是否流式响应
     * @return 构建好的请求体
     */
    private Map<String, Object> buildRequestBody(Integer dialogId, String ask, boolean stream) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个智能助手，需要根据用户的问题提供准确、有用的回答。");
        messages.add(systemMessage);
        
        // 添加历史问答记录作为上下文
        if (dialogId != null) {
            List<QueAns> historyQueAns = queAnsRepository.findByIdDId(dialogId);
            if (historyQueAns != null && !historyQueAns.isEmpty()) {
                // 最多添加最近5轮对话作为上下文，避免token超限
                int startIndex = Math.max(0, historyQueAns.size() - 5);
                for (int i = startIndex; i < historyQueAns.size(); i++) {
                    QueAns queAns = historyQueAns.get(i);
                    
                    // 添加用户问题
                    Map<String, String> historyUserMessage = new HashMap<>();
                    historyUserMessage.put("role", "user");
                    historyUserMessage.put("content", queAns.getQue());
                    messages.add(historyUserMessage);
                    
                    // 添加AI回答
                    Map<String, String> historyAssistantMessage = new HashMap<>();
                    historyAssistantMessage.put("role", "assistant");
                    historyAssistantMessage.put("content", queAns.getAns());
                    messages.add(historyAssistantMessage);
                }
            }
        }
        
        // 当前用户消息
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
     * 支持重试机制，处理网络波动、接口临时不可用的情况
     * @param ask 用户问题
     * @return AI回答
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private String callAI(Integer dialogId, String ask) {
        // 对用户问题做脱敏处理，只记录前50个字符
        String maskedAsk = ask.length() > 50 ? ask.substring(0, 50) + "..." : ask;
        logger.info("开始调用AI API，对话ID: {}, 问题: {}", dialogId, maskedAsk);
        
        // 限流处理
        boolean acquired = rateLimiter.acquirePermission();
        if (!acquired) {
            logger.warn("AI API调用限流，请求被拒绝");
            return "请求过于频繁，请稍后再试。";
        }
        
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(dialogId, ask, false);
            
            // 对请求体中的敏感信息做脱敏处理
            Map<String, Object> maskedRequestBody = new HashMap<>(requestBody);
            if (maskedRequestBody.containsKey("messages")) {
                // 不对messages做详细记录，避免泄露用户问题
                maskedRequestBody.put("messages", "[消息内容已脱敏]");
            }
            logger.info("AI API Request Body: {}", maskedRequestBody);
            
            logger.info("AI API Base URL: {}", baseUrl);
            // 对API Key做脱敏处理
            logger.info("AI API Key: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            
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
            
            // 使用全局 RestTemplate
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
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
                        Object choiceObj = choicesList.getFirst();
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
     * 支持重试机制，处理网络波动、接口临时不可用的情况
     * @param dialogId 对话ID
     * @param ask 用户问题
     * @return 流式回答的Flux
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts=3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private Flux<String> callAIStream(Integer dialogId, String ask) {
        // 对用户问题做脱敏处理，只记录前50个字符
        String maskedAsk = ask.length() > 50 ? ask.substring(0, 50) + "..." : ask;
        logger.info("开始调用AI流式API，对话ID: {}, 问题: {}", dialogId, maskedAsk);
        
        // 限流处理
        boolean acquired = rateLimiter.acquirePermission();
        if (!acquired) {
            logger.warn("AI流式API调用限流，请求被拒绝");
            return Flux.just("请求过于频繁，请稍后再试。");
        }
        
        try {
            // 构建请求体，设置stream: true，真正调用OpenAI API的流式端点
            Map<String, Object> requestBody = buildRequestBody(dialogId, ask, true);
            
            // 构建请求URL
            String url = baseUrl + "/chat/completions";
            
            logger.info("AI API URL: {}", url);
            // 对请求体中的敏感信息做脱敏处理
            Map<String, Object> maskedRequestBody = new HashMap<>(requestBody);
            if (maskedRequestBody.containsKey("messages")) {
                maskedRequestBody.put("messages", "[消息内容已脱敏]");
            }
            logger.info("AI API Request Body: {}", maskedRequestBody);
            
            // 对API Key做脱敏处理
            logger.info("AI API Key: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            
            // 使用RestTemplate的execute方法实现真正的流式调用
            // 将阻塞操作移到boundedElastic线程池中执行，避免阻塞响应式线程
            return Flux.create(sink -> {
                // 在单独的线程中执行阻塞操作
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        // 构建URI对象
                        URI uri = URI.create(url);
                        
                        // 调用API并处理流式响应
                        restTemplate.execute(
                                uri,
                                HttpMethod.POST,
                                requestCallback -> {
                                    // 设置请求头
                                    HttpHeaders requestHeaders = requestCallback.getHeaders();
                                    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
                                    requestHeaders.set("Authorization", "Bearer " + apiKey);
                                    // 写入请求体
                                    objectMapper.writeValue(requestCallback.getBody(), requestBody);
                                },
                                responseExtractor -> {
                                    // 处理流式响应
                                    try (BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(responseExtractor.getBody(), StandardCharsets.UTF_8))) {
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            if (sink.isCancelled()) {
                                                break;
                                            }
                                            // 解析流式响应
                                            String content = parseStreamResponse(line);
                                            if (!content.isEmpty()) {
                                                sink.next(content);
                                            }
                                        }
                                        sink.complete();
                                    } catch (Exception e) {
                                        sink.error(e);
                                    }
                                    return null;
                                }
                        );
                    } catch (Exception e) {
                        sink.error(e);
                    }
                }, java.util.concurrent.Executors.newCachedThreadPool());
            });
        } catch (Exception e) {
            logger.error("Error calling AI stream API: {}", e.getMessage(), e);
            return Flux.just("抱歉，无法获取回答，请稍后再试。");
        }
    }
    
    /**
     * 解析流式响应
     * 处理OpenAI流式响应的data: {JSON}格式，包括空行和结束标记
     * @param responseChunk 响应块
     * @return 解析后的内容
     */
    private String parseStreamResponse(String responseChunk) {
        try {
            // 去除首尾空白
            String trimmedChunk = responseChunk.trim();
            
            // 处理空行
            if (trimmedChunk.isEmpty()) {
                return "";
            }
            
            // 处理结束标记
            if (trimmedChunk.equals("data: [DONE]")) {
                return "";
            }
            
            // 处理data: {JSON}格式
            if (trimmedChunk.startsWith("data: ")) {
                String jsonPart = trimmedChunk.substring(6).trim();
                
                // 解析JSON部分
                if (!jsonPart.isEmpty()) {
                    Map<String, Object> parsed = parseJson(jsonPart);
                    
                    // 提取choices中的content
                    if (parsed.containsKey("choices")) {
                        Object choicesObj = parsed.get("choices");
                        if (choicesObj instanceof List<?> choicesList && !choicesList.isEmpty()) {
                            Object choiceObj = choicesList.getFirst();
                            if (choiceObj instanceof Map<?, ?> choiceMap) {
                                Object deltaObj = choiceMap.get("delta");
                                if (deltaObj instanceof Map<?, ?> deltaMap) {
                                    Object contentObj = deltaMap.get("content");
                                    if (contentObj instanceof String content) {
                                        return content;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            logger.error("Error parsing stream response: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 使用Jackson解析JSON字符串
     * @param json JSON字符串
     * @return 解析后的Map
     */
    private Map<String, Object> parseJson(String json) {
        try {
            // 使用Jackson ObjectMapper解析JSON字符串为Map，添加类型参数避免未检查赋值
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("Error parsing JSON: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
