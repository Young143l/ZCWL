package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Project;
import com.example.zcwl.repository.ProjectRepository;
import com.example.zcwl.service.ProjectService;
import com.example.zcwl.utils.QiniuUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final RestTemplate restTemplate;
    private final QiniuUtil qiniuUtil;
    
    // OpenAI API 配置
    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String openaiApiUrl;
    
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    
    // 临时目录配置
    @Value("${java.io.tmpdir}")
    private String tempDir;
    
    // 缓存，用于存储生成的结果，提高性能
    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, RestTemplate restTemplate, QiniuUtil qiniuUtil) {
        this.projectRepository = projectRepository;
        this.restTemplate = restTemplate;
        this.qiniuUtil = qiniuUtil;
    }
    
    /**
     * 调用AI服务获取回答
     * @param prompt 提示词
     * @return AI生成的回答
     */
    @Retryable(backoff = @org.springframework.retry.annotation.Backoff(delay = 1000))
    private String callAIService(String prompt) {
        String cacheKey = "ai_" + prompt.hashCode();
        
        // 检查缓存
        if (cache.containsKey(cacheKey)) {
            logger.debug("从缓存获取AI回答");
            return cache.get(cacheKey);
        }
        
        try {
            // 构建请求体，确保字符串中的换行符被正确转义
            HttpEntity<String> requestEntity = getStringHttpEntity(prompt);

            // 发送请求
            logger.debug("调用OpenAI API");
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                openaiApiUrl + "/chat/completions", 
                requestEntity, 
                String.class
            );
            
            // 解析响应
            String responseBody = response.getBody();
            logger.debug("OpenAI API响应: {}", responseBody);
            
            // 提取回答内容
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<>() {
            });
            
            if (responseMap.containsKey("choices")) {
                java.util.List<?> choicesList = (java.util.List<?>) responseMap.get("choices");
                if (!choicesList.isEmpty()) {
                    java.util.Map<?, ?> choiceMap = (java.util.Map<?, ?>) choicesList.getFirst();
                    if (choiceMap.containsKey("message")) {
                        java.util.Map<?, ?> messageMap = (java.util.Map<?, ?>) choiceMap.get("message");
                        if (messageMap.containsKey("content")) {
                            String content = messageMap.get("content").toString();
                            
                            // 缓存结果
                            cache.put(cacheKey, content);
                            
                            return content;
                        }
                    }
                }
            }
            
            return "AI服务返回了空结果";
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("OpenAI API调用失败: {}", e.getStatusCode(), e);
            return "AI服务暂时不可用，请稍后重试";
        } catch (Exception e) {
            logger.error("调用AI服务时发生错误: {}", e.getMessage(), e);
            return "处理请求时发生错误";
        }
    }

    private HttpEntity<String> getStringHttpEntity(String prompt) {
        String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        String requestBody = String.format("{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\" }], \"max_tokens\": 100000, \"temperature\": 0.7}", model, escapedPrompt);

        // 设置请求头
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openaiApiKey);

        // 构建请求实体
        return new HttpEntity<>(requestBody, headers);
    }

    @Override
    public List<Project> getProjects(String userId) {
        logger.debug("获取项目列表，用户ID: {}", userId);
        if (userId != null && !userId.isEmpty()) {
            return projectRepository.findByUserId(userId);
        } else {
            return projectRepository.findAll();
        }
    }

    @Override
    public Project createProject(Project project) {
        logger.info("开始创建新项目: {}", project.getProjectName());
        
        // 检查项目名称是否已存在
        Project existingProject = projectRepository.findByProjectNameAndUserId(project.getProjectName(), project.getUserId());
        if (existingProject != null) {
            logger.error("项目名称已存在: {}", project.getProjectName());
            throw new IllegalArgumentException("项目名称已存在");
        }
        
        // 克隆Git项目并上传到云存储
        String projectUrl = project.getUrl();
        if (projectUrl != null && !projectUrl.isEmpty()) {
            logger.info("开始克隆和上传项目: {}", project.getProjectName());
            String cloudStorageId = cloneAndUploadProject(project.getProjectName(), projectUrl);
            
            if (cloudStorageId != null) {
                logger.info("项目克隆上传成功，云存储ID: {}", cloudStorageId);
                project.setCloudStorageId(cloudStorageId);
            } else {
                logger.error("项目克隆上传失败: {}", project.getProjectName());
                throw new RuntimeException("项目克隆上传失败");
            }
        } else {
            logger.warn("项目URL为空，跳过克隆上传: {}", project.getProjectName());
        }
        
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setStatus("active");
        
        Project savedProject = projectRepository.save(project);
        logger.info("项目创建成功，ID: {}", savedProject.getId());
        return savedProject;
    }

    @Override
    public Optional<Project> getProjectById(Long id) {
        logger.debug("获取项目信息，ID: {}", id);
        return projectRepository.findById(id);
    }

    @Override
    public boolean deleteProject(Long id, String userId) {
        logger.debug("删除项目，ID: {}, 用户ID: {}", id, userId);
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            // 验证用户权限
            if (!project.getUserId().equals(userId)) {
                throw new SecurityException("无删除权限");
            }
            projectRepository.delete(project);
            return true;
        }
        return false;
    }

    @Override
    public String askProject(Long id, String question) {
        logger.debug("询问项目问题，项目ID: {}, 问题: {}", id, question);
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("项目不存在");
        }
        Project project = projectOptional.get();
        
        // 构建提示词
        String prompt = String.format("""
                        你是一个专业的项目顾问，针对以下项目回答问题：
                        项目名称: %s
                        项目描述: %s
                        问题: %s
                        请提供详细、专业的回答，包括具体的建议和解决方案。""",
                                    project.getProjectName(),
                                    project.getDescription(),
                                    question);
        
        // 调用AI服务
        return callAIService(prompt);
    }

    @Override
    public String generateProjectDoc(Long id) {
        logger.debug("生成项目学习分析文档，项目ID: {}", id);
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("项目不存在");
        }
        Project project = projectOptional.get();
        
        // 构建提示词
        String prompt = String.format("""
                        请为以下项目生成一份详细的学习分析文档：
                        项目名称: %s
                        项目描述: %s
                        创建时间: %s
                        文档应包含：
                        1. 项目概述
                        2. 技术栈分析
                        3. 学习重点
                        4. 潜在问题与解决方案
                        5. 学习路径建议
                        请使用Markdown格式，内容要专业、详细、有深度。""",
                                    project.getProjectName(),
                                    project.getDescription(),
                                    project.getCreatedAt());
        
        // 调用AI服务
        return callAIService(prompt);
    }

    @Override
    public Flux<String> askProjectStream(Long id, String question) {
        logger.debug("流式处理项目问题，项目ID: {}, 问题: {}", id, question);
        
        // 使用Mono.fromCallable将阻塞的数据库查询包装成非阻塞操作
        return Mono.fromCallable(() -> {
            // 执行阻塞的数据库查询
            Optional<Project> projectOptional = projectRepository.findById(id);
            if (projectOptional.isEmpty()) {
                throw new IllegalArgumentException("项目不存在");
            }
            return projectOptional.get();
        })
        .subscribeOn(Schedulers.boundedElastic()) // 在专门的线程池中执行阻塞操作
        .flatMapMany(project -> {
            // 构建提示词
            String prompt = String.format("""
                            你是一个专业的项目顾问，针对以下项目回答问题：
                            项目名称: %s
                            项目描述: %s
                            问题: %s
                            请提供详细、专业的回答，包括具体的建议和解决方案。""",
                                        project.getProjectName(),
                                        project.getDescription(),
                                        question);
            
            // 调用AI流式服务
            return callAIServiceStream(prompt);
        })
        .onErrorResume(e -> {
            logger.error("处理项目问题失败: {}", e.getMessage(), e);
            return Flux.just("处理项目问题失败: " + e.getMessage());
        });
    }

    @Override
    public Flux<String> generateProjectDocStream(Long id) {
        logger.debug("流式生成项目学习分析文档，项目ID: {}", id);
        
        // 使用Mono.fromCallable将阻塞的数据库查询包装成非阻塞操作
        return Mono.fromCallable(() -> {
            // 执行阻塞的数据库查询
            Optional<Project> projectOptional = projectRepository.findById(id);
            if (projectOptional.isEmpty()) {
                throw new IllegalArgumentException("项目不存在");
            }
            return projectOptional.get();
        })
        .subscribeOn(Schedulers.boundedElastic()) // 在专门的线程池中执行阻塞操作
        .flatMapMany(project -> {
            // 构建提示词
            String prompt = String.format("""
                            请为以下项目生成一份详细的学习分析文档：
                            项目名称: %s
                            项目描述: %s
                            创建时间: %s
                            文档应包含：
                            1. 项目概述
                            2. 技术栈分析
                            3. 学习重点
                            4. 潜在问题与解决方案
                            5. 学习路径建议
                            请使用Markdown格式，内容要专业、详细、有深度。""",
                                        project.getProjectName(),
                                        project.getDescription(),
                                        project.getCreatedAt());
            
            // 调用AI流式服务
            return callAIServiceStream(prompt);
        })
        .onErrorResume(e -> {
            logger.error("生成项目文档失败: {}", e.getMessage(), e);
            return Flux.just("生成项目文档失败: " + e.getMessage());
        });
    }

    /**
     * 调用AI服务获取流式回答
     * @param prompt 提示词
     * @return 流式回答的Flux
     */
    @Retryable(backoff = @org.springframework.retry.annotation.Backoff(delay = 1000))
    private Flux<String> callAIServiceStream(String prompt) {
        try {
            // 构建请求体，确保字符串中的换行符被正确转义
            String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r");
            String requestBody = String.format("{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\" }], \"max_tokens\": 100000, \"temperature\": 0.7, \"stream\": true}", model, escapedPrompt);

            // 构建请求URL
            String url = openaiApiUrl + "/chat/completions";

            // 使用RestTemplate的exchange方法，设置正确的请求头和响应类型
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + openaiApiKey);
            org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity<>(requestBody, headers);

            // 使用Mono.fromCallable将阻塞操作包装成非阻塞操作
            return Mono.fromCallable(() -> {
                // 执行阻塞的HTTP请求
                org.springframework.http.ResponseEntity<java.io.InputStream> responseEntity = restTemplate.exchange(
                        url,
                        org.springframework.http.HttpMethod.POST,
                        requestEntity,
                        java.io.InputStream.class
                );
                return responseEntity.getBody();
            })
            .subscribeOn(Schedulers.boundedElastic()) // 在专门的线程池中执行阻塞操作
            .flatMapMany(inputStream -> {
                // 使用Flux.create来处理流式响应
                return Flux.<String>create(sink -> {
                    // 在线程池中处理输入流
                    Schedulers.boundedElastic().schedule(() -> {
                        java.io.BufferedReader reader = null;
                        try {
                            reader = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8),
                                    131072); // 128KB 缓冲区

                            String line;
                            int lineCount = 0;
                            boolean receivedDone = false;

                            while ((line = reader.readLine()) != null) {
                                lineCount++;

                                if (sink.isCancelled()) {
                                    logger.debug("流被取消，停止处理");
                                    break;
                                }

                                // 解析流式响应
                                String content = parseStreamResponse(line);
                                if (!content.isEmpty()) {
                                    sink.next(content);
                                }

                                // 检查是否是结束标记
                                if (line.trim().equals("data: [DONE]")) {
                                    logger.debug("收到结束标记，停止处理");
                                    receivedDone = true;
                                    break;
                                }
                            }

                            logger.debug("处理完成，共处理 {} 行响应，{}结束标记", lineCount, receivedDone ? "收到" : "未收到");

                            // 确保在完成前没有被取消
                            if (!sink.isCancelled()) {
                                sink.complete();
                                logger.debug("流已成功完成");
                            } else {
                                logger.debug("流已被取消，跳过完成操作");
                            }
                        } catch (Exception e) {
                            logger.error("处理流式响应失败: {}", e.getMessage(), e);
                            if (!sink.isCancelled()) {
                                sink.error(e);
                            }
                        } finally {
                            // 确保资源被正确关闭
                            try {
                                if (reader != null) {
                                    reader.close();
                                }
                                inputStream.close();
                            } catch (Exception e) {
                                logger.error("关闭资源失败: {}", e.getMessage(), e);
                            }
                        }
                    });
                });
            })
            .onErrorResume(e -> {
                logger.error("调用AI流式服务失败: {}", e.getMessage(), e);
                return Flux.just("AI服务暂时不可用，请稍后重试");
            });
        } catch (Exception e) {
            logger.error("调用AI流式服务失败: {}", e.getMessage(), e);
            return Flux.just("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 解析流式响应
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
                        ObjectMapper mapper = new ObjectMapper();
                        java.util.Map<String, Object> responseMap = mapper.readValue(jsonPart, new TypeReference<>() {
                        });
                    
                    if (responseMap.containsKey("choices")) {
                        java.util.List<?> choicesList = (java.util.List<?>) responseMap.get("choices");
                        if (!choicesList.isEmpty()) {
                            // 使用索引0获取第一个元素，兼容所有Java版本
                            java.util.Map<?, ?> choiceMap = (java.util.Map<?, ?>) choicesList.getFirst();
                            if (choiceMap.containsKey("delta")) {
                                java.util.Map<?, ?> deltaMap = (java.util.Map<?, ?>) choiceMap.get("delta");
                                if (deltaMap.containsKey("content")) {
                                    String content = deltaMap.get("content").toString();
                                    if (!content.isEmpty()) {
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
            logger.error("解析流式响应失败: {}", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String cloneAndUploadProject(String projectName, String projectUrl) {
        logger.info("开始克隆和上传项目，项目名称: {}, URL: {}", projectName, projectUrl);
        
        if (projectUrl == null || projectUrl.isEmpty()) {
            logger.error("项目URL为空，项目名称: {}", projectName);
            return null;
        }
        
        // 创建临时目录
        Path tempProjectDir = null;
        Git git = null;
        
        try {
            // 创建临时目录
            tempProjectDir = Files.createTempDirectory(Paths.get(tempDir), "project_" + projectName + "_");
            logger.info("创建临时目录: {}", tempProjectDir);
            
            // 克隆Git仓库
            logger.info("开始克隆Git仓库: {}", projectUrl);
            git = Git.cloneRepository()
                    .setURI(projectUrl)
                    .setDirectory(tempProjectDir.toFile())
                    .call();
            
            logger.info("Git仓库克隆成功，项目: {}", projectName);
            
            // 上传到七牛云
            logger.info("开始上传项目文件到七牛云，项目: {}", projectName);
            boolean uploadSuccess = uploadDirectoryToQiniu(tempProjectDir.toFile(), projectName);
            
            if (uploadSuccess) {
                logger.info("项目文件上传成功，项目: {}，云存储ID: {}", projectName, projectName);
                // 返回云存储ID（使用项目名称作为云存储ID）
                return projectName;
            } else {
                logger.error("项目文件上传失败，项目: {}", projectName);
                return null;
            }
            
        } catch (GitAPIException e) {
            logger.error("克隆Git仓库失败: {}", e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error("文件操作失败: {}", e.getMessage(), e);
            return null;
        } finally {
            // 关闭Git仓库
            if (git != null) {
                git.close();
            }
            
            // 清理临时目录
            if (tempProjectDir != null) {
                try {
                    deleteDirectory(tempProjectDir.toFile());
                    logger.info("临时目录清理完成: {}", tempProjectDir);
                } catch (IOException e) {
                    logger.error("清理临时目录失败: {}", e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * 递归上传目录到七牛云
     * @param directory 目录
     * @param basePath 基础路径（包含项目名称）
     * @return 上传是否成功
     */
    private boolean uploadDirectoryToQiniu(File directory, String basePath) {
        logger.info("开始上传目录: {} 到路径: {}", directory.getName(), basePath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("目录不存在或不是目录: {}", directory.getAbsolutePath());
            return false;
        }
        
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            logger.warn("目录为空: {}", directory.getAbsolutePath());
            return true;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    // 递归处理子目录，构建完整的路径
                    String subDirPath = basePath + "/" + file.getName();
                    boolean subDirSuccess = uploadDirectoryToQiniu(file, subDirPath);
                    if (subDirSuccess) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    // 上传文件，构建完整的文件路径
                    String qiniuPath = basePath + "/" + file.getName();
                    
                    logger.info("上传文件: {} -> {}", file.getName(), qiniuPath);
                    
                    try (FileInputStream fis = new FileInputStream(file)) {
                        String fileUrl = qiniuUtil.uploadFile(fis, qiniuPath);
                        logger.info("文件上传成功: {} -> {}", file.getName(), fileUrl);
                        successCount++;
                    }
                }
            } catch (Exception e) {
                logger.error("上传文件失败: {}, 错误: {}", file.getName(), e.getMessage(), e);
                failCount++;
            }
        }
        
        logger.info("目录上传完成，成功: {}, 失败: {}", successCount, failCount);
        return failCount == 0;
    }
    
    /**
     * 获取相对路径
     * @param baseDir 基础目录
     * @param file 文件
     * @return 相对路径
     */
    private String getRelativePath(File baseDir, File file) {
        String basePath = baseDir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        
        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1).replace("\\", "/");
        }
        
        return file.getName();
    }
    
    /**
     * 递归删除目录
     * @param directory 目录
     * @throws IOException IO异常
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        Files.delete(file.toPath());
                    }
                }
            }
            Files.delete(directory.toPath());
        }
    }
}