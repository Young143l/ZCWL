package com.example.zcwl.service.impl;

import com.example.zcwl.entity.ConsoleProject;
import com.example.zcwl.repository.ConsoleProjectRepository;
import com.example.zcwl.service.ConsoleProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制台应用代码生成项目服务实现类
 */
@Service
public class ConsoleProjectServiceImpl implements ConsoleProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleProjectServiceImpl.class);

    private final ConsoleProjectRepository consoleProjectRepository;
    private final RestTemplate restTemplate;

    // 本地缓存，用于存储AI生成的代码，提高性能
    private final ConcurrentHashMap<String, String> codeCache = new ConcurrentHashMap<>();

    // AI服务配置
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    
    @Value("${spring.ai.openai.chat.options.temperature}")
    private Double temperature;

    @Autowired
    public ConsoleProjectServiceImpl(ConsoleProjectRepository consoleProjectRepository, RestTemplate restTemplate) {
        this.consoleProjectRepository = consoleProjectRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ConsoleProject> getCpProjects(String userId) {
        logger.debug("获取控制台应用代码生成项目列表，用户ID: {}", userId);
        if (userId != null) {
            return consoleProjectRepository.findByUserId(userId);
        } else {
            // 如果userId为null，返回所有项目
            return consoleProjectRepository.findAll();
        }
    }

    @Override
    public ConsoleProject createCpProject(String userId, String projectName, String message, String type) {
        logger.debug("创建新的控制台应用代码生成项目: {}, 用户ID: {}, 类型: {}", projectName, userId, type);
        
        // 检查项目名称是否已存在
        Optional<ConsoleProject> existingProject = consoleProjectRepository.findByProjectNameAndUserId(projectName, userId);
        if (existingProject.isPresent()) {
            throw new IllegalArgumentException("项目名称已存在");
        }
        
        // 生成唯一的cpId
        String cpId = generateCpId();
        
        // 确保类型为小写
        String normalizedType = type.toLowerCase();
        
        // 生成控制台应用代码
        String generatedCode = generateCode(message, normalizedType);
        
        // 创建项目实体
        ConsoleProject project = new ConsoleProject(
                cpId,
                projectName,
                userId,
                generatedCode,
                normalizedType
        );
        
        return consoleProjectRepository.save(project);
    }

    @Override
    public ConsoleProject generateCodeInCpProject(String cpId, String code, String message, List<String> selectId) {
        logger.debug("在项目 {} 中生成新代码，选中的代码块: {}", cpId, selectId);
        
        // 查找项目
        Optional<ConsoleProject> projectOptional = consoleProjectRepository.findByCpId(cpId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("项目不存在");
        }
        
        ConsoleProject project = projectOptional.get();
        
        // 调用AI服务生成代码，传入现有代码作为上下文
        String generatedCode = generateCodeWithExistingCode(code, message, project.getType());
        
        // 更新项目代码
        project.setCode(generatedCode);
        project.setUpdatedAt(LocalDateTime.now());
        
        return consoleProjectRepository.save(project);
    }

    @Override
    public Optional<ConsoleProject> getCpProjectById(String cpId) {
        logger.debug("获取控制台项目 {} 的信息", cpId);
        return consoleProjectRepository.findByCpId(cpId);
    }

    @Override
    public Flux<String> generateCodeStream(String message) {
        logger.debug("流式生成代码: {}", message);
        // 实现流式代码生成逻辑
        return Flux.just("流式生成代码功能开发中");
    }

    @Override
    public Flux<String> generateCodeInCpProjectStream(String cpId, String code, String message) {
        logger.debug("在项目 {} 中流式生成新代码: {}", cpId, message);
        // 实现流式代码生成逻辑
        return Flux.just("流式生成代码功能开发中");
    }

    @Override
    public boolean deleteCpProject(String cpId, String userId) {
        logger.debug("删除控制台应用代码生成项目: {}", cpId);
        
        // 查找项目
        Optional<ConsoleProject> projectOptional = consoleProjectRepository.findByCpId(cpId);
        if (projectOptional.isEmpty()) {
            return false;
        }
        
        ConsoleProject project = projectOptional.get();
        
        // 验证用户权限
        if (!project.getUserId().equals(userId)) {
            throw new SecurityException("无权限删除此项目");
        }
        
        // 删除项目
        consoleProjectRepository.delete(project);
        return true;
    }

    /**
     * 生成唯一的cpId
     * @return 唯一的cpId
     */
    private String generateCpId() {
        return "cp_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成控制台应用代码
     * @param message 提示信息
     * @param type 项目类型
     * @return 生成的代码
     */
    private String generateCode(String message, String type) {
        try {
            // 生成缓存键
            String cacheKey = "code_" + message.hashCode() + "_" + type.hashCode();
            
            // 检查缓存中是否已有结果
            if (codeCache.containsKey(cacheKey)) {
                return codeCache.get(cacheKey);
            }
            
            // 构建完整的AI提示词
            String prompt = buildAIPrompt(message, type);

            // 调用AI服务生成代码
            logger.info("调用AI服务生成控制台应用代码");
            String aiResponse = callAIService(prompt);

            // 将结果存入缓存
            codeCache.put(cacheKey, aiResponse);
            
            return aiResponse;
        } catch (Exception e) {
            logger.error("生成代码失败", e);
            // 如果生成失败，返回默认代码
            return "# 控制台应用\n\n" + message;
        }
    }

    /**
     * 基于现有代码生成新代码
     * @param existingCode 现有代码
     * @param message 提示信息
     * @param type 项目类型
     * @return 生成的代码
     */
    private String generateCodeWithExistingCode(String existingCode, String message, String type) {
        try {
            // 生成缓存键
            String cacheKey = "code_" + existingCode.hashCode() + "_" + message.hashCode() + "_" + type.hashCode();
            
            // 检查缓存中是否已有结果
            if (codeCache.containsKey(cacheKey)) {
                return codeCache.get(cacheKey);
            }
            
            // 构建完整的AI提示词，包含现有代码
            String prompt = buildAIPromptWithExistingCode(existingCode, message, type);

            // 调用AI服务生成代码
            logger.info("调用AI服务生成控制台应用代码");
            String aiResponse = callAIService(prompt);

            // 将结果存入缓存
            codeCache.put(cacheKey, aiResponse);
            
            return aiResponse;
        } catch (Exception e) {
            logger.error("生成代码失败", e);
            // 如果生成失败，返回现有代码
            return existingCode;
        }
    }

    /**
     * 构建AI提示词
     * @param message 提示信息
     * @param type 项目类型
     * @return 构建好的提示词
     */
    private String buildAIPrompt(String message, String type) {
        return "# Role: 资深后端工程师 (Senior Backend Engineer)\n" +
               "\n" +
               "## Profile\n" +
               "你是一位精通控制台应用开发的资深工程师，擅长各种编程语言和框架的控制台应用开发。\n" +
               "\n" +
               "## Task Goals\n" +
               "基于用户需求，生成高质量的控制台应用代码。\n" +
               "\n" +
               "## Input Context\n" +
               "* **用户需求**：`" + message + "`\n" +
               "* **项目类型**：`" + type + "`\n" +
               "\n" +
               "## Output Format\n" +
               "直接输出完整的控制台应用代码，不需要任何解释或标记。";
    }

    /**
     * 构建包含现有代码的AI提示词
     * @param existingCode 现有代码
     * @param message 提示信息
     * @param type 项目类型
     * @return 构建好的提示词
     */
    private String buildAIPromptWithExistingCode(String existingCode, String message, String type) {
        return "# Role: 资深后端工程师 (Senior Backend Engineer)\n" +
               "\n" +
               "## Profile\n" +
               "你是一位精通控制台应用开发的资深工程师，擅长各种编程语言和框架的控制台应用开发。\n" +
               "\n" +
               "## Task Goals\n" +
               "基于用户需求和现有代码，生成或修改控制台应用代码。\n" +
               "\n" +
               "## Input Context\n" +
               "* **现有代码**：`" + existingCode + "`\n" +
               "* **用户需求**：`" + message + "`\n" +
               "* **项目类型**：`" + type + "`\n" +
               "\n" +
               "## Output Format\n" +
               "直接输出完整的控制台应用代码，不需要任何解释或标记。";
    }

    /**
     * 调用AI服务
     * @param prompt 提示词
     * @return AI生成的代码
     * @throws Exception 异常
     */
    private String callAIService(String prompt) throws Exception {
        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", temperature
        );

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 创建请求
        RequestEntity<Map<String, Object>> request = RequestEntity
                .post(URI.create(baseUrl + "/chat/completions"))
                .headers(headers)
                .body(requestBody);

        // 发送请求
        ResponseEntity<Map> response = restTemplate.exchange(request, Map.class);

        // 解析响应
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.getFirst();
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    String content = (String) message.get("content");
                    
                    // 清理Markdown代码块标记
                    content = content.replaceAll("^```[\\w\\s]*\\n", "");
                    content = content.replaceAll("\\n```$", "");
                    content = content.trim();
                    
                    return content;
                }
            }
        }

        throw new Exception("AI服务调用失败");
    }
}
