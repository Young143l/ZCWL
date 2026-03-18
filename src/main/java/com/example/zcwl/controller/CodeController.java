package com.example.zcwl.controller;

import com.example.zcwl.entity.SimpleFrontendProject;
import com.example.zcwl.entity.ConsoleProject;
import com.example.zcwl.service.SimpleFrontendProjectService;
import com.example.zcwl.service.ConsoleProjectService;
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
    private final ConsoleProjectService consoleProjectService;

    @Autowired
    public CodeController(SimpleFrontendProjectService simpleFrontendProjectService, ConsoleProjectService consoleProjectService) {
        this.simpleFrontendProjectService = simpleFrontendProjectService;
        this.consoleProjectService = consoleProjectService;
    }

    /**
     * 获取全部的代码生成项目的列表，可指定用户
     * 接口：GET /code/?uId="uid"
     * @param uId 用户ID
     * @return 项目列表
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getCodeProjects(
            @RequestParam(value = "uId", required = false) String uId) {
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
        response.put("uId", uId);
        response.put("list", list);
        return response;
    }



    /**
     * 获取用户的简单前端代码生成项目列表
     * 接口：GET /code/sf?uId={用户id}
     * @param uId 用户ID
     * @return 项目列表
     */
    @GetMapping("/sf")
    public ResponseEntity<Map<String, Object>> getSfProjectsByUserId(
            @RequestParam("uId") String uId) {
        logger.debug("获取用户 {} 的简单前端代码生成项目列表", uId);
        try {
            List<SimpleFrontendProject> projects = simpleFrontendProjectService.getSfProjects(uId);
            Map<String, Object> response = new HashMap<>();
            response.put("projects", projects);
            response.put("uId", uId);
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
            Object uIdObj = request.get("uId");
            if (uIdObj == null || uIdObj.toString().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "uId参数不能为空");
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
        response.put("sfId", createdProject.getSfId());
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
     * 响应：成功(200 OK)：{"sfId":"", "name":"", "code":{"html":"", "css":"", "javascript":""}}
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
                response.put("sfId", project.getSfId());
                response.put("name", project.getProjectName());
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

    /**
     * 删除指定ID的简单前端代码生成项目
     * 接口：GET /code/sf/delete/:id
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 响应：成功(200 OK)：{"status":"success", "message":"项目删除成功", "id":"项目ID"}
     */
    @GetMapping("/sf/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteSfProject(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("删除简单前端代码生成项目: {}", id);
        try {
            // 验证路径参数
            if (id == null || id.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 获取当前用户ID
            String userId = authentication.getName();
            
            // 执行删除操作
            boolean deleted = simpleFrontendProjectService.deleteSfProject(id, userId);
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
            errorResponse.put("error", "删除项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取用户的控制台项目列表
     * 接口：GET /code/cp?uId={用户id}
     * @param uId 用户ID
     * @return 项目列表
     */
    @GetMapping("/cp")
    public ResponseEntity<Map<String, Object>> getCpProjects(
            @RequestParam("uId") String uId) {
        logger.debug("获取控制台项目列表，用户ID: {}", uId);
        try {
            List<ConsoleProject> projects = consoleProjectService.getCpProjects(uId);
            // 转换为文档要求的格式
            List<Map<String, String>> list = new ArrayList<>();
            for (ConsoleProject project : projects) {
                Map<String, String> item = new HashMap<>();
                item.put("name", project.getProjectName());
                item.put("id", project.getCpId());
                item.put("type", "console"); // 类型固定为console
                list.add(item);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("uId", uId);
            response.put("list", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取控制台项目列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取项目列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新的控制台项目
     * 接口：POST /code/cp
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 请求体：{"uId":"", "projectName":"", "message":""}
     * 响应：成功(200 OK)：{"cpId":"", "code":""}
     */
    @PostMapping("/cp")
    public ResponseEntity<Map<String, Object>> createCpProject(
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication) {
        logger.debug("创建新的控制台项目: {}", request);
        try {
            // 验证请求参数
            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求体不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 验证必要参数
            Object uIdObj = request.get("uId");
            if (uIdObj == null || uIdObj.toString().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "uId参数不能为空");
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
            ConsoleProject createdProject = consoleProjectService.createCpProject(userId, projectName, message);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("cpId", createdProject.getCpId());
            response.put("code", createdProject.getCode() != null ? createdProject.getCode() : "");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("创建控制台项目失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            logger.error("创建控制台项目失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "创建项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 在指定ID的控制台项目中生成新的代码
     * 接口：POST /code/cp/:id
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 请求体：{"code":"", "message":"", "selectId":["",""]}
     * 响应：成功(200 OK)：{"cpId":"", "code":""}
     */
    @PostMapping("/cp/{id}")
    public ResponseEntity<Map<String, Object>> generateCodeInCpProject(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        logger.debug("在控制台项目 {} 中生成新代码: {}", id, request);
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
            if (!request.containsKey("message") || request.get("message") == null || ((String) request.get("message")).trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "message参数不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String code = request.containsKey("code") && request.get("code") != null ? request.get("code").toString() : "";
            String message = (String) request.get("message");
            List<String> selectId = (List<String>) request.get("selectId");
            if (selectId == null) {
                selectId = new ArrayList<>();
            }
            
            // 调用服务层生成代码
            ConsoleProject updatedProject = consoleProjectService.generateCodeInCpProject(id, code, message, selectId);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("cpId", updatedProject.getCpId());
            response.put("code", updatedProject.getCode() != null ? updatedProject.getCode() : "");
            return ResponseEntity.ok(response);
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
     * 获取指定ID的控制台项目信息
     * 接口：GET /code/cp/:id
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 响应：成功(200 OK)：{"cpId":"", "name":"", "code":""}
     */
    @GetMapping("/cp/{id}")
    public ResponseEntity<Map<String, Object>> getCpProjectById(
            @PathVariable String id) {
        logger.debug("获取控制台项目 {} 的信息", id);
        try {
            // 验证路径参数
            if (id == null || id.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Optional<ConsoleProject> projectOptional = consoleProjectService.getCpProjectById(id);
            if (projectOptional.isPresent()) {
                ConsoleProject project = projectOptional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("cpId", project.getCpId());
                response.put("name", project.getProjectName());
                response.put("code", project.getCode() != null ? project.getCode() : "");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("获取控制台项目信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取项目信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除指定ID的控制台项目
     * 接口：GET /code/cp/delete/:id
     * 请求头：Content-Type: application/json, Authorization: "Bearer token"
     * 响应：成功(200 OK)：{"status":"success", "message":"项目删除成功", "id":"项目ID"}
     */
    @GetMapping("/cp/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteCpProject(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("删除控制台项目: {}", id);
        try {
            // 验证路径参数
            if (id == null || id.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "项目ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 获取当前用户ID
            String userId = authentication.getName();
            
            // 执行删除操作
            boolean deleted = consoleProjectService.deleteCpProject(id, userId);
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
            errorResponse.put("error", "删除项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
