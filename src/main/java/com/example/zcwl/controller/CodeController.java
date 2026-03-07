package com.example.zcwl.controller;

import com.example.zcwl.entity.SimpleFrontendProject;
import com.example.zcwl.service.SimpleFrontendProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.ArrayList;

/**
 * 代码生成控制器
 * 实现REST接口
 */
@RestController
@RequestMapping("/code")
public class CodeController {

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private final SimpleFrontendProjectService simpleFrontendProjectService;

    @Autowired
    public CodeController(SimpleFrontendProjectService simpleFrontendProjectService) {
        this.simpleFrontendProjectService = simpleFrontendProjectService;
    }

    /**
     * 获取全部的代码生成项目的列表，可指定用户
     * 接口：GET /code/projects?u_id="uid"
     * @param uId 用户ID
     * @return 项目列表
     */
    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> getCodeProjects(
            @RequestParam(value = "u_id", required = false) String uId) {
        logger.debug("获取代码生成项目列表，用户ID: {}", uId);
        try {
            List<SimpleFrontendProject> projects = simpleFrontendProjectService.getSfProjects(uId);
            // 转换为文档要求的格式
            Map<String, Object> response = getStringObjectMap(uId, projects);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取项目列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取项目列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private static Map<String, Object> getStringObjectMap(String uId, List<SimpleFrontendProject> projects) {
        List<Map<String, String>> list = new ArrayList<>();
        for (SimpleFrontendProject project : projects) {
            Map<String, String> item = new HashMap<>();
            item.put("name", project.getProjectName());
            item.put("id", project.getSfId());
            item.put("type", "simple_frontend"); // 类型固定为simple_frontend
            list.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("u_id", uId);
        response.put("list", list);
        return response;
    }

    /**
     * 获取全部的代码生成项目的列表，可指定用户（兼容测试脚本）
     * 接口：GET /code?u_id="uid"
     * @param uId 用户ID
     * @return 项目列表
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getCodeProjectsRoot(
            @RequestParam(value = "u_id", required = false) String uId) {
        logger.debug("获取代码生成项目列表（根路径），用户ID: {}", uId);
        return getCodeProjects(uId);
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
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication) {
        logger.debug("创建新的简单前端代码生成项目: {}", request);
        try {
            // 验证请求参数
            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求体不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 验证必要参数
            Object uIdObj = request.get("u_id");
            if (uIdObj == null || uIdObj.toString().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "u_id参数不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Object projectNameObj = request.get("projectName");
            if (projectNameObj == null || projectNameObj.toString().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "projectName参数不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Object messageObj = request.get("message");
            if (messageObj == null || messageObj.toString().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "message参数不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 获取当前用户ID
            String userId = authentication.getName();
            
            // 确保用户ID一致
            String requestUserId = uIdObj.toString();
            if (!userId.equals(requestUserId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "用户ID不匹配");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String projectName = projectNameObj.toString();
            String message = messageObj.toString();
            
            // 调用服务层创建项目
            SimpleFrontendProject createdProject = simpleFrontendProjectService.createSfProject(userId, projectName, message);
            
            // 构建响应
            return getMapResponseEntity(createdProject);
        } catch (IllegalArgumentException e) {
            logger.error("创建项目失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            logger.error("创建项目失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "创建项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private ResponseEntity<Map<String, Object>> getMapResponseEntity(SimpleFrontendProject createdProject) {
        Map<String, Object> response = new HashMap<>();
        response.put("sf_id", createdProject.getSfId());
        response.put("code", Map.of(
                "html", createdProject.getHtml(),
                "css", createdProject.getCss(),
                "javascript", createdProject.getJavascript()
        ));

        return ResponseEntity.ok(response);
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
            // 验证路径参数
            if (id == null || id.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 验证请求体
            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求体不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 验证必要参数
            if (!request.containsKey("code") || request.get("code") == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "code参数不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (!request.containsKey("message") || request.get("message") == null || ((String) request.get("message")).trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "message参数不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Map<String, String> code = (Map<String, String>) request.get("code");
            // 处理前端可能传递的js字段
            if (code != null) {
                if (code.containsKey("js") && !code.containsKey("javascript")) {
                    code.put("javascript", code.get("js"));
                }
            } else {
                code = new HashMap<>();
            }
            
            String message = (String) request.get("message");
            List<String> selectId = (List<String>) request.get("selectId");
            if (selectId == null) {
                selectId = new ArrayList<>();
            }
            
            // 调用服务层生成代码
            SimpleFrontendProject updatedProject = simpleFrontendProjectService.generateCodeInSfProject(id, code, message, selectId);
            
            // 构建响应
            return getMapResponseEntity(updatedProject);
        } catch (IllegalArgumentException e) {
            logger.error("生成代码失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("生成代码失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "生成代码失败: " + e.getMessage());
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
            // 验证路径参数
            if (id == null || id.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Optional<SimpleFrontendProject> projectOptional = simpleFrontendProjectService.getSfProjectById(id);
            if (projectOptional.isPresent()) {
                SimpleFrontendProject project = projectOptional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("sf_id", project.getSfId());
                response.put("code", Map.of(
                        "html", project.getHtml() != null ? project.getHtml() : "",
                        "css", project.getCss() != null ? project.getCss() : "",
                        "javascript", project.getJavascript() != null ? project.getJavascript() : ""
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
            errorResponse.put("error", "获取项目信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 流式生成代码
     * 接口：POST /code/stream
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 请求体：{"message":""}
     * 响应：流式文本响应
     */
    @PostMapping("/stream")
    public Flux<String> generateCodeStream(
            @RequestBody Map<String, Object> request) {
        logger.debug("流式生成代码: {}", request);
        try {
            // 验证请求参数
            if (request == null || !request.containsKey("message") || request.get("message") == null || ((String) request.get("message")).trim().isEmpty()) {
                return Flux.just("message参数不能为空");
            }
            
            String message = (String) request.get("message");
            return simpleFrontendProjectService.generateCodeStream(message);
        } catch (Exception e) {
            logger.error("流式生成代码失败", e);
            return Flux.just("生成代码失败: " + e.getMessage());
        }
    }

    /**
     * 在指定ID的项目中流式生成新的代码
     * 接口：POST /code/sf/:id/stream
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 请求体：{"code":{"html":"", "css":"", "js":""}, "message":"", "selectId":["",""]}
     * 响应：流式文本响应
     */
    @PostMapping("/sf/{id}/stream")
    public Flux<String> generateCodeInSfProjectStream(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        logger.debug("在项目 {} 中流式生成新代码: {}", id, request);
        try {
            // 验证路径参数
            if (id == null || id.trim().isEmpty()) {
                return Flux.just("项目ID不能为空");
            }
            
            // 验证请求体
            if (request == null) {
                return Flux.just("请求体不能为空");
            }
            
            // 验证必要参数
            if (!request.containsKey("code") || request.get("code") == null) {
                return Flux.just("code参数不能为空");
            }
            
            if (!request.containsKey("message") || request.get("message") == null || ((String) request.get("message")).trim().isEmpty()) {
                return Flux.just("message参数不能为空");
            }
            
            Map<String, String> code = (Map<String, String>) request.get("code");
            // 处理前端可能传递的js字段
            if (code != null) {
                if (code.containsKey("js") && !code.containsKey("javascript")) {
                    code.put("javascript", code.get("js"));
                }
            } else {
                code = new HashMap<>();
            }
            
            String message = (String) request.get("message");
            List<String> selectId = (List<String>) request.get("selectId");
            if (selectId == null) {
                selectId = new ArrayList<>();
            }
            
            return simpleFrontendProjectService.generateCodeInSfProjectStream(id, code, message, selectId);
        } catch (Exception e) {
            logger.error("流式生成代码失败", e);
            return Flux.just("生成代码失败: " + e.getMessage());
        }
    }

}
