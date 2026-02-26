package com.example.zcwl.controller;

import com.example.zcwl.entity.SimpleFrontendProject;
import com.example.zcwl.service.SimpleFrontendProjectService;
import com.example.zcwl.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 代码生成控制器
 * 实现REST接口
 */
@RestController
@RequestMapping("/code")
public class CodeController {

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private final SimpleFrontendProjectService simpleFrontendProjectService;
    private final AiChatService aiChatService;

    @Autowired
    public CodeController(SimpleFrontendProjectService simpleFrontendProjectService, AiChatService aiChatService) {
        this.simpleFrontendProjectService = simpleFrontendProjectService;
        this.aiChatService = aiChatService;
    }

    /**
     * 获取用户的简单前端代码生成项目列表
     * 接口：GET /code/sf?u_id={用户id}
     * @param uId 用户ID
     * @return 项目列表
     */
    @GetMapping("/sf")
    public ResponseEntity<Map<String, Object>> getSfProjectsByUserId(
            @RequestParam("u_id") String uId) {
        logger.debug("获取用户 {} 的简单前端代码生成项目列表", uId);
        try {
            List<SimpleFrontendProject> projects = simpleFrontendProjectService.getSfProjects(uId);
            Map<String, Object> response = new HashMap<>();
            response.put("projects", projects);
            response.put("u_id", uId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取项目列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取项目列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新的简单前端代码生成项目
     * 接口：POST /code/sf
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 请求体：{"u_id":"", "projectName":"", "message":""}
     * 响应：成功(200 OK)：{"sf_id":"", "code":{"html":"", "css":"", "javascript":""}}
     */
    @PostMapping("/sf")
    public ResponseEntity<Map<String, Object>> createSfProject(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        logger.debug("创建新的简单前端代码生成项目: {}", request);
        try {
            // 获取当前用户ID
            String userId = authentication.getName();
            
            // 验证请求参数
            if (!request.containsKey("u_id") || !request.containsKey("projectName") || !request.containsKey("message")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求参数不完整，需要u_id、projectName和message");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 确保用户ID一致
            String requestUserId = (String) request.get("u_id");
            if (!userId.equals(requestUserId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "用户ID不匹配");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String projectName = (String) request.get("projectName");
            String message = (String) request.get("message");
            
            // 调用服务层创建项目
            SimpleFrontendProject createdProject = simpleFrontendProjectService.createSfProject(userId, projectName, message);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("sf_id", createdProject.getSfId());
            response.put("code", Map.of(
                    "html", createdProject.getHtml(),
                    "css", createdProject.getCss(),
                    "javascript", createdProject.getJavascript()
            ));
            
            return ResponseEntity.ok(response);
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
     * 在指定ID的项目中发起AI对话生成新的代码
     * 接口：POST /code/sf/:id
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 请求体：{"code":{"html":"", "css":"", "js":""}, "message":"", "selectId":["",""]}
     * 响应：成功(200 OK)：{"sf_id":"", "code":{"html":"", "css":"", "javascript":""}}
     */
    @PostMapping("/sf/{id}")
    public ResponseEntity<Map<String, Object>> generateCodeInSfProject(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        logger.debug("在项目 {} 中生成新代码: {}", id, request);
        try {
            // 验证请求参数
            if (!request.containsKey("code") || !request.containsKey("message")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求参数不完整，需要code和message");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Map<String, String> code = (Map<String, String>) request.get("code");
            // 处理前端可能传递的js字段
            if (code.containsKey("js") && !code.containsKey("javascript")) {
                code.put("javascript", code.get("js"));
            }
            String message = (String) request.get("message");
            List<String> selectId = (List<String>) request.get("selectId");
            
            // 调用服务层生成代码
            SimpleFrontendProject updatedProject = simpleFrontendProjectService.generateCodeInSfProject(id, code, message, selectId);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("sf_id", updatedProject.getSfId());
            response.put("code", Map.of(
                    "html", updatedProject.getHtml(),
                    "css", updatedProject.getCss(),
                    "javascript", updatedProject.getJavascript()
            ));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("生成代码失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("生成代码失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "生成代码失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取指定ID的简易前端项目相关信息
     * 接口：GET /code/sf/:id
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 响应：成功(200 OK)：{"sf_id":"", "code":{"html":"", "css":"", "javascript":""}}
     */
    @GetMapping("/sf/{id}")
    public ResponseEntity<Map<String, Object>> getSfProjectById(
            @PathVariable String id) {
        logger.debug("获取简易前端项目 {} 的信息", id);
        try {
            Optional<SimpleFrontendProject> projectOptional = simpleFrontendProjectService.getSfProjectById(id);
            if (projectOptional.isPresent()) {
                SimpleFrontendProject project = projectOptional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("sf_id", project.getSfId());
                response.put("code", Map.of(
                        "html", project.getHtml(),
                        "css", project.getCss(),
                        "javascript", project.getJavascript()
                ));
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


}
