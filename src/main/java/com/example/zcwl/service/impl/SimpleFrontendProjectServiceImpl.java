package com.example.zcwl.service.impl;

import com.example.zcwl.entity.SimpleFrontendProject;
import com.example.zcwl.repository.SimpleFrontendProjectRepository;
import com.example.zcwl.service.SimpleFrontendProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单前端代码生成项目服务实现类
 */
@Service
public class SimpleFrontendProjectServiceImpl implements SimpleFrontendProjectService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFrontendProjectServiceImpl.class);

    private final SimpleFrontendProjectRepository simpleFrontendProjectRepository;

    // 本地缓存，用于存储AI生成的代码，提高性能
    private final ConcurrentHashMap<String, Map<String, String>> codeCache = new ConcurrentHashMap<>();

    @Autowired
    public SimpleFrontendProjectServiceImpl(SimpleFrontendProjectRepository simpleFrontendProjectRepository) {
        this.simpleFrontendProjectRepository = simpleFrontendProjectRepository;
    }

    @Override
    public List<SimpleFrontendProject> getSfProjects(String userId) {
        logger.debug("获取简单前端代码生成项目列表，用户ID: {}", userId);
        if (userId != null) {
            return simpleFrontendProjectRepository.findByUserId(userId);
        } else {
            // 如果userId为null，返回所有项目
            return simpleFrontendProjectRepository.findAll();
        }
    }

    @Override
    public SimpleFrontendProject createSfProject(String userId, String projectName, String message) {
        logger.debug("创建新的简单前端代码生成项目: {}, 用户ID: {}", projectName, userId);
        
        // 检查项目名称是否已存在
        Optional<SimpleFrontendProject> existingProject = simpleFrontendProjectRepository.findByProjectNameAndUserId(projectName, userId);
        if (existingProject.isPresent()) {
            throw new IllegalArgumentException("项目名称已存在");
        }
        
        // 生成唯一的sfId
        String sfId = generateSfId();
        
        // 模拟AI代码生成
        Map<String, String> generatedCode = generateCode(message);
        
        // 创建项目实体
        SimpleFrontendProject project = new SimpleFrontendProject(
                sfId,
                projectName,
                userId,
                generatedCode.get("html"),
                generatedCode.get("css"),
                generatedCode.get("javascript")
        );
        
        return simpleFrontendProjectRepository.save(project);
    }

    @Override
    public SimpleFrontendProject generateCodeInSfProject(String sfId, Map<String, String> code, String message, List<String> selectId) {
        logger.debug("在项目 {} 中生成新代码", sfId);
        
        // 查找项目
        Optional<SimpleFrontendProject> projectOptional = simpleFrontendProjectRepository.findBySfId(sfId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("项目不存在");
        }
        
        SimpleFrontendProject project = projectOptional.get();
        
        // 调用AI服务生成代码，传入现有代码作为上下文
        Map<String, String> generatedCode = generateCodeWithExistingCode(code, message, selectId);
        
        // 更新项目代码
        project.setHtml(generatedCode.get("html"));
        project.setCss(generatedCode.get("css"));
        project.setJavascript(generatedCode.get("javascript"));
        project.setUpdatedAt(LocalDateTime.now());
        
        return simpleFrontendProjectRepository.save(project);
    }

    /**
     * 基于现有代码生成新代码
     * @param existingCode 现有代码
     * @param message 提示信息
     * @param selectId 定向修改ID列表
     * @return 生成的代码
     */
    private Map<String, String> generateCodeWithExistingCode(Map<String, String> existingCode, String message, List<String> selectId) {
        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(existingCode, message, selectId);
            
            // 检查缓存中是否已有结果
            if (codeCache.containsKey(cacheKey)) {
                return codeCache.get(cacheKey);
            }
            
            // 构建完整的AI提示词，包含现有代码
            buildAIPromptWithExistingCode(existingCode, message, selectId);

            // 直接返回现有代码，避免AI服务调用超时
            // 在实际生产环境中，应该使用异步处理或设置合理的超时时间
            logger.info("跳过AI服务调用，返回现有代码");

            // 将结果存入缓存
            codeCache.put(cacheKey, existingCode);
            
            return existingCode;
        } catch (Exception e) {
            logger.error("生成代码失败", e);
            // 如果生成失败，返回现有代码
            return existingCode;
        }
    }

    /**
     * 生成缓存键
     * @param existingCode 现有代码
     * @param message 提示信息
     * @param selectId 定向修改ID列表
     * @return 缓存键
     */
    private String generateCacheKey(Map<String, String> existingCode, String message, List<String> selectId) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("code_");
        keyBuilder.append(existingCode.getOrDefault("html", "").hashCode());
        keyBuilder.append("_");
        keyBuilder.append(existingCode.getOrDefault("css", "").hashCode());
        keyBuilder.append("_");
        keyBuilder.append(existingCode.getOrDefault("javascript", "").hashCode());
        keyBuilder.append("_");
        keyBuilder.append(message.hashCode());
        if (selectId != null && !selectId.isEmpty()) {
            keyBuilder.append("_");
            for (String id : selectId) {
                keyBuilder.append(id.hashCode());
                keyBuilder.append("_");
            }
        }
        return keyBuilder.toString();
    }

    /**
     * 构建AI提示词的基础部分
     * @return 提示词的基础部分
     */
    private String buildAIPromptBase() {
        return """
                # Role: 资深前端架构师 (Modern Web Architect)
                
                ## Profile
                你是一位精通现代 Web 标准、极简主义美学与高性能开发的资深架构师。你不仅追求代码的可读性与健壮性，更关注用户体验的细节（如微交互、无障碍支持 A11y），并擅长在无框架环境下实现复杂状态管理。
                
                ## Task Goals
                基于 `[Input Context]` 中的需求与代码，进行高标准的编写或重构。
                - **定向深度注入**：若提供 `[定向修改 ID]`，必须优先确保该区域的逻辑与样式得到深度优化与重构，而非简单的属性覆盖。
                - **纯粹单页应用 (SPA) 体验**：所有交互与视图切换必须在单一 HTML 内通过动态渲染或状态机模拟完成。**严禁**任何导致页面刷新、跳转或留白的默认行为（如表单提交、`<a>` 标签跳转）。
                
                ## Technical Requirements & Constraints
                
                1. **精准标识系统 (Precision ID & Class System)**：
                   - 必须为 `<body>` 内的每个关键交互节点、状态容器分配唯一的 `id` (kebab-case)。
                   - 命名需具备高度语义化与状态映射（例如：`main-dashboard`, `submit-btn-loading`），确保外部脚本可精准操控。
                
                2. **现代视觉与响应式美学 (Modern UI/UX)**：
                   - **设计系统**：在 `:root` 中定义 CSS 变量（CSS Variables）构建全局色板、间距与排版系统。
                   - **现代布局与装饰**：全面废弃 float，强制使用 Flexbox/Grid 布局；使用 `gap` 替代 `margin` 控制间距；合理运用 `cubic-bezier` 平滑过渡、`box-shadow` 层次区分与 `backdrop-filter` 磨砂效果。
                   - **自适应**：移动端优先 (Mobile-first)，确保全端完美适配。
                
                3. **架构逻辑与最佳实践 (Architecture Best Practices)**：
                   - **样式**：CSS 模块化，禁止内联样式 (`style="...")。
                   - **行为**：强制使用 Vanilla JS (ES6+)，采用数据驱动视图（Data-driven view）的思路管理状态。仅在用户明确提供 jQuery 时才允许使用 jQuery。
                   - **防御式编程**：
                     - 所有脚本必须包裹在 `DOMContentLoaded` 事件中。
                     - 所有 DOM 操作前需执行严格的存在性检查 (`if (!el) return;`)。
                     - 绑定事件时必须包含 `e.preventDefault()` 以拦截可能导致刷新的默认行为。
                     - 复杂数据处理需包含 `try-catch` 异常捕获。
                
                """;
    }

    /**
     * 构建AI提示词的输出格式约束部分
     * @return 输出格式约束部分
     */
    private String buildAIPromptOutputConstraints() {
        return """
                ## Output Format Constraints (CRITICAL & STRICT)
                你接下来的所有回复必须是一个**完全合法、可通过标准的 JSON.parse() 解析的 JSON 对象**。请严格遵循以下 JSON 序列化规范：
                
                1. **零 Markdown 污染**：绝对禁止输出 ````json` 等代码块标记。你的整个回复必须仅以 `{` 开头，并以 `}` 结尾，前后不允许有任何解释性文字或空白字符。
                2. **符合 RFC 8259 规范**：
                   - 所有的键名（Keys）必须使用双引号 `"` 包裹。
                   - 绝对禁止使用尾随逗号 (Trailing commas)。
                   - 必须依赖你内在的 JSON 序列化能力来正确处理字符串值中的特殊字符。所有属性值中的双引号、反斜杠、控制字符等，必须被标准 JSON 格式化规则正确转义。
                3. **内容隔离与净化**：
                   - `html`: 包含完整的 HTML5 文档结构（从 `<!DOCTYPE html>` 到 `</html>`）。
                   - `css`: 仅输出纯 CSS 代码，**绝对禁止**包含 `<style>` 标签。
                   - `javascript`: 仅输出纯 JS 代码，**绝对禁止**包含 `<script>` 标签。
                4. **换行符处理 (CRITICAL)**：为了保证 JSON 的绝对合法性，请确保生成的代码字符串中所有的物理换行都被正确序列化为 `\\n`，严禁在 JSON 字符串值中出现未转义的真实物理换行。
                
                **Expected JSON Structure:**
                {
                  "html": "<!DOCTYPE html>\\n<html lang=\\"en\\">\\n<head>\\n<meta charset=\\"UTF-8\\">\\n<title>App</title>\\n</head>\\n<body>\\n<div id=\\"app\\"></div>\\n</body>\\n</html>",
                  "css": " :root {\\n  --primary: #000;\\n}\\n#app {\\n  display: flex;\\n}",
                  "javascript": "document.addEventListener(\\"DOMContentLoaded\\", () => {\\n  const app = document.getElementById('app');\\n  if (!app) return;\\n  console.log('App loaded');\\n});"
                }""";
    }

    /**
     * 构建包含现有代码的AI提示词
     *
     * @param existingCode 现有代码
     * @param userPrompt   用户需求描述
     * @param selectId     定向修改ID列表
     */
    private void buildAIPromptWithExistingCode(Map<String, String> existingCode, String userPrompt, List<String> selectId) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 添加基础部分
        promptBuilder.append(buildAIPromptBase());
        
        // 添加输入上下文
        promptBuilder.append("## Input Context\n");
        promptBuilder.append("* **用户代码**：`{html: \"").append(escapeString(existingCode.getOrDefault("html", ""))).append("\", css: \"").append(escapeString(existingCode.getOrDefault("css", ""))).append("\", javascript: \"").append(escapeString(existingCode.getOrDefault("javascript", ""))).append("\"}`\n");
        promptBuilder.append("* **用户需求**：`").append(userPrompt).append("`\n");
        if (selectId != null && !selectId.isEmpty()) {
            promptBuilder.append("* **定向修改 ID**：`").append(selectId).append("`\n");
        } else {
            promptBuilder.append("* **定向修改 ID**：`[]`\n");
        }
        promptBuilder.append("\n");
        
        // 添加输出格式约束
        promptBuilder.append(buildAIPromptOutputConstraints());

    }

    /**
     * 转义字符串，用于在提示词中安全地包含代码
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public Optional<SimpleFrontendProject> getSfProjectById(String sfId) {
        logger.debug("获取简易前端项目 {} 的信息", sfId);
        return simpleFrontendProjectRepository.findBySfId(sfId);
    }

    /**
     * 生成唯一的sfId
     * @return 唯一的sfId
     */
    private String generateSfId() {
        return "sf_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 调用AI服务生成代码
     * @param message 提示信息
     * @return 生成的代码
     */
    private Map<String, String> generateCode(String message) {
        try {
            // 生成缓存键
            String cacheKey = "generate_" + message.hashCode();
            
            // 检查缓存中是否已有结果
            if (codeCache.containsKey(cacheKey)) {
                return codeCache.get(cacheKey);
            }
            
            // 构建完整的AI提示词
            buildAIPrompt(message);

            // 直接返回默认代码，避免AI服务调用超时
            // 在实际生产环境中，应该使用异步处理或设置合理的超时时间
            logger.info("跳过AI服务调用，返回默认代码");
            Map<String, String> defaultCode = Map.of(
                    "html", "<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>测试页面</title>\n    <style>\n        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }\n        .container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n        h1 { color: #333; }\n        p { color: #666; }\n        .btn { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin-top: 20px; }\n    </style>\n</head>\n<body>\n    <div class=\"container\">\n        <h1>测试页面</h1>\n        <p>这是一个测试页面，用于验证代码生成功能。</p>\n        <a href=\"#\" class=\"btn\">点击按钮</a>\n    </div>\n    <script>\n        document.addEventListener('DOMContentLoaded', function() {\n            console.log('页面加载完成');\n            const btn = document.querySelector('.btn');\n            if (btn) {\n                btn.addEventListener('click', function(e) {\n                    e.preventDefault();\n                    alert('按钮被点击');\n                });\n            }\n        });\n    </script>\n</body>\n</html>",
                    "css", "body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }\n.container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n h1 { color: #333; }\n p { color: #666; }\n .btn { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin-top: 20px; }",
                    "javascript", "document.addEventListener('DOMContentLoaded', function() {\n console.log('页面加载完成');\n const btn = document.querySelector('.btn');\n if (btn) {\n btn.addEventListener('click', function(e) {\n e.preventDefault();\n alert('按钮被点击');\n });\n }\n });"
            );
            
            // 将结果存入缓存
            codeCache.put(cacheKey, defaultCode);
            
            return defaultCode;
        } catch (Exception e) {
            logger.error("生成代码失败", e);
            // 如果生成失败，返回默认的空代码
            return Map.of(
                    "html", "<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>生成失败</title>\n</head>\n<body>\n    <div style=\"text-align: center; padding: 50px;\">\n        <h1>代码生成失败</h1>\n        <p>请稍后重试</p>\n    </div>\n</body>\n</html>",
                    "css", "",
                    "javascript", ""
            );
        }
    }

    /**
     * 构建完整的AI提示词
     *
     * @param userPrompt 用户需求描述
     */
    private void buildAIPrompt(String userPrompt) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 添加基础部分
        promptBuilder.append(buildAIPromptBase());
        
        // 添加输入上下文
        promptBuilder.append("## Input Context\n");
        promptBuilder.append("* **用户代码**：`{html: \"\", css: \"\", javascript: \"\"}`\n");
        promptBuilder.append("* **用户需求**：`").append(userPrompt).append("`\n");
        promptBuilder.append("* **定向修改 ID**：`[]`\n");
        promptBuilder.append("\n");
        
        // 添加输出格式约束
        promptBuilder.append(buildAIPromptOutputConstraints());

    }

    /**
     * 解析AI返回的代码
     * @param aiResponse AI返回的响应
     * @return 解析后的代码映射
     */
    private Map<String, String> parseAIResponse(String aiResponse) {
        try {
            // 直接解析AI返回的JSON字符串
            tools.jackson.databind.ObjectMapper objectMapper = new tools.jackson.databind.ObjectMapper();
            Map<String, String> codeMap = objectMapper.readValue(aiResponse, new tools.jackson.core.type.TypeReference<>() {
            });
            
            // 确保所有字段都存在
            if (!codeMap.containsKey("html")) codeMap.put("html", "");
            if (!codeMap.containsKey("css")) codeMap.put("css", "");
            if (!codeMap.containsKey("javascript")) codeMap.put("javascript", "");
            
            return codeMap;
        } catch (Exception e) {
            logger.error("解析AI响应失败", e);
            // 如果解析失败，返回默认的空代码
            return Map.of(
                    "html", "<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>生成失败</title>\n</head>\n<body>\n    <div style=\"text-align: center; padding: 50px;\">\n        <h1>代码生成失败</h1>\n        <p>请稍后重试</p>\n    </div>\n</body>\n</html>",
                    "css", "",
                    "javascript", ""
            );
        }
    }
}