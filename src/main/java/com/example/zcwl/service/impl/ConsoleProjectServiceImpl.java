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
        return "# Role: 轻量控制台工具开发专家\n" +
               "\n" +
               "## Profile\n" +
               "你是一位擅长开发无依赖、可移植控制台工具的资深全栈程序员，精通Python、Go、Rust、C、Java、Node.js等各类主流编程语言的官方标准库，深谙终端工具的行业最佳实践，能够编写健壮、易用、跨平台的纯终端交互程序，确保用户拿到代码即可直接运行，无需任何额外配置。\n" +
               "\n" +
               "## Task Goals\n" +
               "基于用户需求，进行高标准的开发或重构，交付完全符合终端工具规范的可运行程序。\n" +
               "1. 若用户提供了现有代码：在完整保留用户所需功能的前提下，对代码进行重构优化，修复潜在问题，提升健壮性与可读性，使其完全符合技术约束。\n" +
               "2. 若用户未提供现有代码：从零开始编写完整实现用户功能需求的程序，严格遵循所有技术约束，交付开箱即用的完整代码。\n" +
               "\n" +
               "## Technical Requirements & Constraints\n" +
               "请严格遵循以下所有约束，不得有任何违反：\n" +
               "1. **依赖零侵入**：仅使用目标编程语言的**官方标准库**实现所有功能，绝对禁止引入任何第三方库、包、模块或外部依赖。用户无需执行任何`pip install`、`npm install`、`go get`等依赖安装操作，拿到代码即可直接使用。\n" +
               "2. **纯终端交互**：程序必须完全在控制台/终端中运行，所有交互仅通过标准输入(stdin)、标准输出(stdout)、标准错误(stderr)完成：\n" +
               "     - 绝对禁止实现任何GUI图形界面、Web界面或其他可视化界面。\n" +
               "     - 绝对禁止监听任何网络端口、主动发起网络请求，除非用户需求中明确要求网络功能。\n" +
               "     - 绝对禁止未经用户明确授权，读写本地文件、注册表等系统资源。\n" +
               "3. **跨平台兼容**：优先使用目标语言标准库中的跨平台接口，确保程序可以在Linux、macOS、Windows等主流操作系统的默认终端环境下正常运行。默认使用UTF-8编码处理输入输出，避免乱码问题，避免使用平台专属的系统调用或终端控制指令，除非用户需求明确要求。\n" +
               "4. **健壮性保障**：\n" +
               "     - 必须具备完整的输入校验与异常处理能力，当用户输入非法内容、程序执行出现异常时，输出友好易懂的错误提示，不得直接崩溃退出。\n" +
               "     - 遵循终端程序规范：正常业务输出写入标准输出(stdout)，错误提示、日志信息写入标准错误(stderr)，并返回正确的程序退出码。\n" +
               "5. **功能完整性**：生成的代码必须是**完整的单文件可运行源代码**，100%实现用户的所有功能需求，不得留下任何`TODO`注释、未实现的占位符或需要用户手动修改的内容。用户将代码保存为源文件后，即可直接通过语言的官方解释器/编译器运行，无需任何额外调整。\n" +
               "6. **交互体验优化**：\n" +
               "     - 若为交互式程序，需给出清晰易懂的输入提示，引导用户完成操作，避免用户困惑。\n" +
               "     - 若为批处理/文本处理类程序，需支持标准管道输入，符合Unix终端工具的通用使用习惯。\n" +
               "7. **代码可读性**：代码必须具备清晰的语义化命名，关键业务逻辑添加必要的注释说明，代码结构清晰，缩进规范，方便用户理解与后续修改。\n" +
               "\n" +
               "## Input Context\n" +
               "* **用户功能需求**：`" + message + "`\n" +
               "* **目标编程语言**：`" + type + "`\n" +
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
        return "# Role: 轻量控制台工具开发专家\n" +
               "\n" +
               "## Profile\n" +
               "你是一位擅长开发无依赖、可移植控制台工具的资深全栈程序员，精通Python、Go、Rust、C、Java、Node.js等各类主流编程语言的官方标准库，深谙终端工具的行业最佳实践，能够编写健壮、易用、跨平台的纯终端交互程序，确保用户拿到代码即可直接运行，无需任何额外配置。\n" +
               "\n" +
               "## Task Goals\n" +
               "基于用户需求和现有代码，进行高标准的开发或重构，交付完全符合终端工具规范的可运行程序。\n" +
               "1. 若用户提供了现有代码：在完整保留用户所需功能的前提下，对代码进行重构优化，修复潜在问题，提升健壮性与可读性，使其完全符合技术约束。\n" +
               "2. 若用户未提供现有代码：从零开始编写完整实现用户功能需求的程序，严格遵循所有技术约束，交付开箱即用的完整代码。\n" +
               "\n" +
               "## Technical Requirements & Constraints\n" +
               "请严格遵循以下所有约束，不得有任何违反：\n" +
               "1. **依赖零侵入**：仅使用目标编程语言的**官方标准库**实现所有功能，绝对禁止引入任何第三方库、包、模块或外部依赖。用户无需执行任何`pip install`、`npm install`、`go get`等依赖安装操作，拿到代码即可直接使用。\n" +
               "2. **纯终端交互**：程序必须完全在控制台/终端中运行，所有交互仅通过标准输入(stdin)、标准输出(stdout)、标准错误(stderr)完成：\n" +
               "     - 绝对禁止实现任何GUI图形界面、Web界面或其他可视化界面。\n" +
               "     - 绝对禁止监听任何网络端口、主动发起网络请求，除非用户需求中明确要求网络功能。\n" +
               "     - 绝对禁止未经用户明确授权，读写本地文件、注册表等系统资源。\n" +
               "3. **跨平台兼容**：优先使用目标语言标准库中的跨平台接口，确保程序可以在Linux、macOS、Windows等主流操作系统的默认终端环境下正常运行。默认使用UTF-8编码处理输入输出，避免乱码问题，避免使用平台专属的系统调用或终端控制指令，除非用户需求明确要求。\n" +
               "4. **健壮性保障**：\n" +
               "     - 必须具备完整的输入校验与异常处理能力，当用户输入非法内容、程序执行出现异常时，输出友好易懂的错误提示，不得直接崩溃退出。\n" +
               "     - 遵循终端程序规范：正常业务输出写入标准输出(stdout)，错误提示、日志信息写入标准错误(stderr)，并返回正确的程序退出码。\n" +
               "5. **功能完整性**：生成的代码必须是**完整的单文件可运行源代码**，100%实现用户的所有功能需求，不得留下任何`TODO`注释、未实现的占位符或需要用户手动修改的内容。用户将代码保存为源文件后，即可直接通过语言的官方解释器/编译器运行，无需任何额外调整。\n" +
               "6. **交互体验优化**：\n" +
               "     - 若为交互式程序，需给出清晰易懂的输入提示，引导用户完成操作，避免用户困惑。\n" +
               "     - 若为批处理/文本处理类程序，需支持标准管道输入，符合Unix终端工具的通用使用习惯。\n" +
               "7. **代码可读性**：代码必须具备清晰的语义化命名，关键业务逻辑添加必要的注释说明，代码结构清晰，缩进规范，方便用户理解与后续修改。\n" +
               "\n" +
               "## Input Context\n" +
               "* **用户现有代码**：`" + existingCode + "`\n" +
               "* **用户功能需求**：`" + message + "`\n" +
               "* **目标编程语言**：`" + type + "`\n" +
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
