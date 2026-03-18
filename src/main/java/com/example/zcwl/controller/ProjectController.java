package com.example.zcwl.controller;

import com.example.zcwl.entity.Project;
import com.example.zcwl.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 项目管理控制器
 * 实现REST接口
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 获取项目列表
     * 接口：GET /project/?u_id=
     * @param uId 用户ID（可选）
     * @return 项目列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProjectsByUserId(
            @RequestParam(value = "u_id", required = false) String uId) {
        logger.debug("获取项目列表，用户ID: {}", uId);
        try {
            List<Project> projects = projectService.getProjects(uId);
            Map<String, Object> response = new HashMap<>();
            response.put("projects", projects);
            if (uId != null) {
                response.put("u_id", uId);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取项目列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取项目列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新项目
     * 接口：POST /project/
     * @param request 请求参数
     * @param authentication 认证信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        logger.debug("创建新项目: {}", request);
        try {
            // 获取当前用户ID
            String userId = authentication.getName();
            
            // 验证请求参数
            if (!request.containsKey("projectName")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目名称是必填项");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 创建项目实体
            Project project = new Project();
            project.setProjectName((String) request.get("projectName"));
            project.setDescription((String) request.get("description"));
            project.setUserId(userId);
            
            // 调用服务层创建项目
            Project createdProject = projectService.createProject(project);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdProject.getId());
            response.put("projectName", createdProject.getProjectName());
            response.put("description", createdProject.getDescription());
            response.put("createdAt", createdProject.getCreatedAt());
            response.put("status", "success");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("创建项目失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            logger.error("创建项目失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "创建项目失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取指定ID的项目信息
     * 接口：GET /project/{id}
     * @param id 项目ID
     * @return 项目信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectById(
            @PathVariable Long id) {
        logger.debug("获取项目 {} 的信息", id);
        try {
            Optional<Project> projectOptional = projectService.getProjectById(id);
            if (projectOptional.isPresent()) {
                Project project = projectOptional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", project.getId());
                response.put("projectName", project.getProjectName());
                response.put("description", project.getDescription());
                response.put("userId", project.getUserId());
                response.put("createdAt", project.getCreatedAt());
                response.put("updatedAt", project.getUpdatedAt());
                response.put("status", project.getStatus());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("获取项目信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取项目信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除指定ID的项目
     * 接口：GET /project/delet/{id}
     * @param id 项目ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @GetMapping("/delet/{id}")
    public ResponseEntity<Map<String, Object>> deleteProject(
            @PathVariable Long id,
            Authentication authentication) {
        logger.debug("删除项目 {}", id);
        try {
            // 获取当前用户ID
            String userId = authentication.getName();
            
            boolean deleted = projectService.deleteProject(id, userId);
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", id);
                response.put("status", "success");
                response.put("message", "项目删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (SecurityException e) {
            logger.error("删除项目失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            logger.error("删除项目失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "删除项目失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 询问该项目相关
     * 接口：POST /project/ask/{id}
     * @param id 项目ID
     * @param request 请求参数
     * @return 询问结果
     */
    @PostMapping("/ask/{id}")
    public ResponseEntity<Map<String, Object>> askProject(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.debug("询问项目 {}: {}", id, request);
        try {
            // 验证请求参数
            if (!request.containsKey("question")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "问题内容是必填项");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String question = request.get("question");
            String answer = projectService.askProject(id, question);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("answer", answer);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("询问项目失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("询问项目失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "询问项目失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 生成该项目相关的学习分析文档
     * 接口：GET /project/doc/{id}
     * @param id 项目ID
     * @return 学习分析文档
     */
    @GetMapping("/doc/{id}")
    public ResponseEntity<Map<String, Object>> generateProjectDoc(
            @PathVariable Long id) {
        logger.debug("生成项目 {} 的学习分析文档", id);
        try {
            String doc = projectService.generateProjectDoc(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("doc", doc);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("生成文档失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("生成文档失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "生成文档失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 流式处理项目相关问题
     * 接口：POST /project/ask/{id}/stream
     * @param id 项目ID
     * @param request 请求参数
     * @return 流式问题解答
     */
    @PostMapping("/ask/{id}/stream")
    public Flux<String> askProjectStream(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.debug("流式处理项目 {}: {}", id, request);
        try {
            // 验证请求参数
            if (!request.containsKey("question")) {
                return Flux.just("问题内容是必填项");
            }
            
            String question = request.get("question");
            return projectService.askProjectStream(id, question);
        } catch (IllegalArgumentException e) {
            logger.error("询问项目失败: {}", e.getMessage());
            return Flux.just(e.getMessage());
        } catch (Exception e) {
            logger.error("询问项目失败", e);
            return Flux.just("询问项目失败: " + e.getMessage());
        }
    }

    /**
     * 流式生成该项目相关的学习分析文档
     * 接口：GET /project/doc/{id}/stream
     * @param id 项目ID
     * @return 流式学习分析文档
     */
    @GetMapping("/doc/{id}/stream")
    public Flux<String> generateProjectDocStream(
            @PathVariable Long id) {
        logger.debug("流式生成项目 {} 的学习分析文档", id);
        try {
            return projectService.generateProjectDocStream(id);
        } catch (IllegalArgumentException e) {
            logger.error("生成文档失败: {}", e.getMessage());
            return Flux.just(e.getMessage());
        } catch (Exception e) {
            logger.error("生成文档失败", e);
            return Flux.just("生成文档失败: " + e.getMessage());
        }
    }
}
