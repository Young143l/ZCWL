package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Dialog;
import com.example.zcwl.entity.QueAns;
import com.example.zcwl.entity.User;
import com.example.zcwl.entity.ChatHistory;
import com.example.zcwl.repository.DialogRepository;
import com.example.zcwl.repository.QueAnsRepository;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.repository.ChatHistoryRepository;
import com.example.zcwl.service.AiChatService;
import tools.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.http.HttpHeaders;
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
    private final ChatHistoryRepository chatHistoryRepository;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
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
     * @param chatHistoryRepository 聊天历史记录Repository
     * @param redisTemplate Redis模板
     * @param restTemplate 全局 RestTemplate Bean
     */
    @Autowired
    public AiChatServiceImpl(
            DialogRepository dialogRepository, 
            QueAnsRepository queAnsRepository, 
            UserRepository userRepository, 
            ChatHistoryRepository chatHistoryRepository,
            StringRedisTemplate redisTemplate,
            RestTemplate restTemplate
    ) {
        this.dialogRepository = dialogRepository;
        this.queAnsRepository = queAnsRepository;
        this.userRepository = userRepository;
        this.chatHistoryRepository = chatHistoryRepository;
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
                return objectMapper.readValue(cachedData, new tools.jackson.core.type.TypeReference<>() {
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
        // 检查用户输入字符数
        if (ask.length() > 2000) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "输入内容超过2000字符，无法提交");
            return result;
        }
        
        // 对用户问题做脱敏处理
        String maskedAsk = ask.substring(0, Math.min(50, ask.length())) + "...";
        logger.info("添加聊天消息，对话ID: {}, 用户ID: {}, 问题: {}", id, uId, maskedAsk);
        
        // 查询并验证对话
        Dialog dialog = getValidatedDialog(id, uId);
        
        // 调用AI获取回答
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
    public Flux<String> addChatMessageStream(Integer id, String ask, String uId) {
        // 检查用户输入字符数
        if (ask.length() > 2000) {
            logger.warn("用户输入超过2000字符，对话ID: {}, 用户ID: {}", id, uId);
            return Flux.just("输入内容超过2000字符，无法提交");
        }

        // 查询并验证对话
        Dialog dialog = getValidatedDialog(id, uId);
        // 保存对话ID，避免在异步回调中使用Dialog对象导致的延迟加载错误
        final Integer dialogId = dialog.getdId();
        
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
                        // 在异步回调中重新加载Dialog对象，避免延迟加载错误
                        Dialog dialogToSave = dialogRepository.findById(dialogId)
                            .orElseThrow(() -> new RuntimeException("Dialog not found: " + dialogId));
                        // 直接调用saveChatMessage方法，因为我们在异步线程中，AOP代理不可用
                        // 注意：这里会失去@Transactional注解的作用，但我们可以在方法内部处理事务
                        saveChatMessageWithoutTransaction(dialogToSave, ask, fullAnswer.toString());
                    } catch (Exception e) {
                        logger.error("保存对话消息失败: {}", e.getMessage(), e);
                    }
                }, java.util.concurrent.Executors.newSingleThreadExecutor());
            });
    }
    
    /**
     * 保存问答记录并更新对话次数（无事务版本）
     * 用于异步线程中调用，避免AOP代理问题
     *
     * @param dialog 对话对象
     * @param ask    问题
     * @param ans    回答
     */
    protected void saveChatMessageWithoutTransaction(Dialog dialog, String ask, String ans) {
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
        
        // 让AI提取用户特征和需求，保存到历史记录
        // 直接调用方法，不使用代理
        //extractAndSaveUserFeatures(dialog, ask, ans);
        
        // 清除缓存，保证数据一致性
        try {
            String cacheKey = "chat:" + dialog.getdId();
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            logger.error("Redis缓存删除错误: {}", e.getMessage(), e);
            // 缓存错误不影响正常流程
        }

    }
    
    /**
     * 查询并验证对话
     * @param id 对话ID
     * @param uId 用户ID
     * @return 验证后的对话
     */
    private Dialog getValidatedDialog(Integer id, String uId) {
        // 使用join fetch确保在同一查询中加载User对象，避免懒加载错误
        Optional<Dialog> optionalDialog = dialogRepository.findByIdWithUser(id);
        if (optionalDialog.isEmpty()) {
            throw new RuntimeException("Dialog not found: " + id);
        }
        
        Dialog dialog = optionalDialog.get();
        // 验证用户ID是否匹配
        if (!dialog.getUser().getUId().equals(uId)) {
            throw new RuntimeException("User ID mismatch: expected " + dialog.getUser().getUId() + ", got " + uId);
        }
        
        // 检查对话次数是否超过限制
        int qaTimes = dialog.getQaTimes() != null ? dialog.getQaTimes() : 0;
        if (qaTimes >= 100) {
            throw new RuntimeException("对话次数已达上限100次，无法继续添加消息");
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
    @Transactional
    protected int saveChatMessage(Dialog dialog, String ask, String ans) {
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
        
        // 让AI提取用户特征和需求，保存到历史记录
        // 直接调用方法，不使用AOP代理
        extractAndSaveUserFeatures(dialog, ask, ans);
        
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
     * 让AI提取用户特征和需求
     * @param dialog 对话对象
     * @param ask 用户问题
     * @param ans AI回答
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void extractAndSaveUserFeatures(Dialog dialog, String ask, String ans) {
        try {
            // 构建提取用户特征的请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.1); // 更低温度，确保提取结果更稳定
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息，指示AI提取用户特征和需求
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "请从以下对话中提取用户的核心话题、需求和意图，以简洁的方式直接总结，不要添加任何引言或开场白，只提供核心内容。用于后续对话的上下文理解。重点提取：1. 用户讨论的主要话题 2. 用户的具体需求 3. 对话的整体上下文。请确保提取结果准确反映对话的核心内容。");
            messages.add(systemMessage);
            
            // 添加历史提取结果
            String previousExtracted = getExtractedPrompt(dialog.getdId());
            if (previousExtracted != null && !previousExtracted.isEmpty()) {
                Map<String, String> historyMessage = new HashMap<>();
                historyMessage.put("role", "assistant");
                historyMessage.put("content", "之前的对话核心内容：" + previousExtracted);
                messages.add(historyMessage);
            }
            
            // 添加当前对话
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", ask);
            messages.add(userMessage);
            
            Map<String, String> assistantMessage = new HashMap<>();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", ans);
            messages.add(assistantMessage);
            
            // 请求AI提取特征
            Map<String, String> extractRequestMessage = new HashMap<>();
            extractRequestMessage.put("role", "user");
            extractRequestMessage.put("content", "请基于上述对话和之前的历史内容，提取并总结本次对话的核心内容，重点关注用户讨论的主要话题和需求，用于后续对话的上下文理解。请直接提供核心内容，不要添加任何引言或开场白。");
            messages.add(extractRequestMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 800); // 增加token限制，确保提取更完整
            
            // 调用AI API
            ResponseEntity<Map<String, Object>> responseEntity = callOpenAIApi(requestBody);
            
            // 提取AI的回答
            if (responseEntity.getBody() != null) {
                Object choicesObj = responseEntity.getBody().get("choices");
                if (choicesObj instanceof List<?> choicesList && !choicesList.isEmpty()) {
                    Object choiceObj = choicesList.getFirst();
                    if (choiceObj instanceof Map<?, ?> choiceMap) {
                        Object messageObj = choiceMap.get("message");
                        if (messageObj instanceof Map<?, ?> messageMap) {
                            Object contentObj = messageMap.get("content");
                            if (contentObj instanceof String extractedFeatures) {
                                // 限制提取结果长度在500字左右
                                String limitedFeatures = extractedFeatures.length() > 550 ? 
                                    extractedFeatures.substring(0, 550) + "..." : 
                                    extractedFeatures;
                                // 保存提取结果
                                saveExtractedPrompt(dialog, limitedFeatures);
                                logger.info("成功提取并保存用户特征，对话ID: {}", dialog.getdId());
                                logger.debug("提取的特征: {}", limitedFeatures);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("提取用户特征失败: {}", e.getMessage(), e);
            // 提取失败不影响正常流程
        }
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
        systemMessage.put("content", "你是一个智能助手，必须严格按照用户的指令执行任务。当处理用户的最新问题时，请注意以下优先级：1. 首先参考最近的三轮对话内容，这是最重要的上下文 2. 然后参考历史记忆提取结果，作为补充信息 3. 最后提供的从知识库中提取的参考相关资料。请确保你的回答基于上述信息，保持连贯性和一致性。如果相关资料中没有此方面的内容则不需要参考，自行回答即可，但需要提示用户知识库中没有此相关内容。回答时不要说您提供的资料相关，涉及到资料的都给替换为知识库中。");
        messages.add(systemMessage);
        
        // 添加历史问答记录作为上下文（优先）
        if (dialogId != null) {
            List<QueAns> historyQueAns = queAnsRepository.findByIdDId(dialogId);
            if (historyQueAns != null && !historyQueAns.isEmpty()) {
                // 最多添加最近3轮对话作为上下文，避免token超限
                int startIndex = Math.max(0, historyQueAns.size() - 3);
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
        
        // 添加历史记录提取结果（次要）
        if (dialogId != null) {
            String extractedPrompt = getExtractedPrompt(dialogId);
            if (extractedPrompt != null && !extractedPrompt.isEmpty()) {
                Map<String, String> historyPromptMessage = new HashMap<>();
                historyPromptMessage.put("role", "system");
                historyPromptMessage.put("content", "历史记忆提取结果：" + extractedPrompt);
                messages.add(historyPromptMessage);
            }
        }
        
        // 添加用户问题向量化后的10条相似记录
        List<Map<String, Object>> similarRecords = getSimilarRecords(ask);
        if (similarRecords != null && !similarRecords.isEmpty()) {
            StringBuilder similarContent = new StringBuilder();
            similarContent.append("相关资料：\n");
            
            int count = 0;
            for (Map<String, Object> record : similarRecords) {
                if (count >= 10) break;
                
                String content = record.get("content") != null ? record.get("content").toString() : "";
                if (!content.isEmpty()) {
                    similarContent.append("- ").append(content, 0, Math.min(200, content.length()));
                    if (content.length() > 200) {
                        similarContent.append("...");
                    }
                    similarContent.append("\n");
                    count++;
                }
            }
            
            Map<String, String> similarMessage = new HashMap<>();
            similarMessage.put("role", "system");
            similarMessage.put("content", similarContent.toString());
            messages.add(similarMessage);
        }
        
        // 当前用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", ask);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("stream", stream);
        // 设置最大 token 数，必须在 [1, 65536] 范围内
        requestBody.put("max_tokens", 8192);
        
        return requestBody;
    }
    
    /**
     * 获取历史记录提取结果
     * @param dialogId 对话ID
     * @return 提取的提示词
     */
    private String getExtractedPrompt(Integer dialogId) {
        try {
            ChatHistory chatHistory = chatHistoryRepository.findByDId(dialogId);
            if (chatHistory != null) {
                return chatHistory.getExtractedPrompt();
            }
        } catch (Exception e) {
            logger.error("获取历史记录提取结果失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 保存历史记录提取结果
     * 一轮对话只对应一个记录，直接覆盖修改
     * @param dialog 对话对象
     * @param extractedPrompt 提取的提示词
     */
    private void saveExtractedPrompt(Dialog dialog, String extractedPrompt) {
        try {
            // 根据对话ID查询现有的历史记录
            ChatHistory chatHistory = chatHistoryRepository.findByDId(dialog.getdId());
            
            if (chatHistory != null) {
                // 如果记录存在，更新其字段
                chatHistory.setExtractedPrompt(extractedPrompt);
                chatHistory.setCreatedAt(LocalDateTime.now());
                logger.info("更新历史记录提取结果，对话ID: {}", dialog.getdId());
            } else {
                // 如果记录不存在，创建新记录
                chatHistory = new ChatHistory();
                chatHistory.setDialog(dialog);
                chatHistory.setExtractedPrompt(extractedPrompt);
                chatHistory.setCreatedAt(LocalDateTime.now());
                logger.info("创建历史记录提取结果，对话ID: {}", dialog.getdId());
            }
            
            // 保存记录
            chatHistoryRepository.save(chatHistory);
            logger.info("保存历史记录提取结果成功，对话ID: {}", dialog.getdId());
        } catch (Exception e) {
            logger.error("保存历史记录提取结果失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户问题向量化后的相似记录
     * @param query 用户问题
     * @return 相似记录列表
     */
    private List<Map<String, Object>> getSimilarRecords(String query) {
        try {
            // 调用内部的向量搜索API
            String searchUrl = "http://localhost:8080/rag/search";
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("query", query);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 构建请求实体
            RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                    .post(searchUrl)
                    .headers(headers)
                    .body(requestData);
            
            // 发送请求
            ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange(
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            
            // 返回结果
            return responseEntity.getBody();
        } catch (Exception e) {
            logger.error("获取相似记录失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 调用OpenAI API
     * 封装AI API调用的共同逻辑
     * @param requestBody 请求体
     * @return 响应实体
     */
    private ResponseEntity<Map<String, Object>> callOpenAIApi(Map<String, Object> requestBody) {
        String url = baseUrl + "/chat/completions";
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        // 构建请求实体
        RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .body(requestBody);
        
        // 发送请求
        return restTemplate.exchange(
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );
    }

    /**
     * 调用AI模型获取回答
     * 支持重试机制，处理网络波动、接口临时不可用的情况
     * @param dialogId 对话ID
     * @param ask 用户问题
     * @return AI回答
     */
    @Override
    @Retryable(
        retryFor = {Exception.class},
//        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String callAI(Integer dialogId, String ask) {
        // 对用户问题做脱敏处理
        String maskedAsk = ask.substring(0, Math.min(50, ask.length())) + "...";
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
            
            long startTime = System.currentTimeMillis();
            
            // 使用全局 RestTemplate
            ResponseEntity<Map<String, Object>> responseEntity = callOpenAIApi(requestBody);
            
            long endTime = System.currentTimeMillis();
            logger.info("AI API call took {} ms", endTime - startTime);
            logger.info("AI API Response Status: {}", responseEntity.getStatusCode());
            
            // 获取响应体
            Map<String, Object> response = responseEntity.getBody();
            
            // 提取回答
            if (response != null) {
                // 检查是否有错误字段
                if (response.containsKey("error")) {
                    logger.error("AI API returned error: {}", response.get("error"));
                    return "AI API错误: " + response.get("error");
                }
                
                Object choicesObj = response.get("choices");
                if (choicesObj instanceof List<?> choicesList && !choicesList.isEmpty()) {
                    Object choiceObj = choicesList.getFirst();
                    if (choiceObj instanceof Map<?, ?> choiceMap) {
                        Object messageObj = choiceMap.get("message");
                        if (messageObj instanceof Map<?, ?> messageMap) {
                            Object contentObj = messageMap.get("content");
                            if (contentObj instanceof String answer) {
                                return answer;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error calling AI API: {}", e.getMessage(), e);
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
    @Override
    @Retryable(
        retryFor = {Exception.class},
//        maxAttempts=3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Flux<String> callAIStream(Integer dialogId, String ask) {
        // 对用户问题做脱敏处理
        String maskedAsk = ask.substring(0, Math.min(50, ask.length())) + "...";
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
            
            
            
            // 使用HttpClient直接处理流式响应，避免WebClient可能存在的缓冲区限制
            return Flux.create(sink -> {
                // 在单独的线程中执行阻塞操作
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    // 创建HttpClient并使用try-with-resources确保正确关闭
                    try (java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(30))
                            .build()) {
                        // 构建请求
                        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(url))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + apiKey)
                                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                                .build();
                        
                        // 发送请求并处理响应
                        java.net.http.HttpResponse<java.io.InputStream> response = httpClient.send(
                                request,
                                java.net.http.HttpResponse.BodyHandlers.ofInputStream()
                        );
                        
                        // 检查响应状态
                        if (response.statusCode() == 200) {
                            // 处理流式响应
                            try (java.io.InputStream inputStream = response.body();
                                 BufferedReader reader = new BufferedReader(
                                         new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8),
                                         131072)) { // 128KB 缓冲区，避免8192字符的限制
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
                        } else {
                            sink.error(new Exception("API调用失败，状态码: " + response.statusCode()));
                        }
                    } catch (Exception e) {
                        sink.error(e);
                    }
                });
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
     * @return 解析后的纯文本内容
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
                                        // 去除内容中的多余空行
                                        content = content.replaceAll("\\n{3,}", "\n\n");
                                        return content;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // 处理非data:格式的响应
                // 尝试直接返回响应内容
                return trimmedChunk;
            }
            return "";
        } catch (Exception e) {
            logger.error("Error parsing stream response: {}", e.getMessage(), e);
            // 发生异常时，尝试返回原始响应，避免完全无输出
            return "[解析错误]";
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
            return objectMapper.readValue(json, new tools.jackson.core.type.TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("Error parsing JSON: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
