package com.example.zcwl.controller;

import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.entity.User;
import com.example.zcwl.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器类
 * <p>
 * 这个类的作用是处理与认证相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现用户登录和注册功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/开头
// 例如：http://localhost:8080/login
@RequestMapping("/")


public class AuthController {

    // 注入认证服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final AuthService authService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将认证服务对象注入到控制器中
     * @param authService 认证服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建AuthController对象时，自动将AuthService的实例传递给这个构造函数
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 登录接口已移至UserController，路径为/users/login

    /**
     * 用户注册
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/register
     * - 请求体：JSON格式的注册数据，例如：
     *   {
     *     "username": "张三",
     *     "password": "123456",
     *     "email": "zhangsan@example.com"
     *   }
     * - 响应：注册成功返回用户信息，状态码201；注册失败返回错误信息，状态码400
     *
     * @param userDTO 用户注册数据，由@RequestBody注解自动转换
     * @return 包含注册结果的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/register时，会调用这个方法
    @PostMapping("/register")
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> register(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果userDTO对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为UserDTO对象
            @RequestBody UserDTO userDTO) {
        try {
            // 调用服务层的方法进行注册
            User user = authService.register(userDTO);
            
            // 返回注册成功的用户数据和201状态码
            // HttpStatus.CREATED表示资源创建成功
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // 注册失败，返回400状态码和标准化的错误响应
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // 其他异常，返回500状态码和标准化的错误响应
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", "服务器内部错误，请稍后重试");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 验证token
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/validate-token
     * - 请求头：Authorization: Bearer token
     *   或者：URL查询参数?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * - 响应：token有效返回状态码200和token信息；token无效返回状态码401和错误信息
     *
     * @param token JWT token，从URL查询参数中获取
     * @param authorization Authorization头，格式为Bearer token
     * @return 包含验证结果的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/validate-token时，会调用这个方法
    @GetMapping("/validate-token")
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> validateToken(
            // @RequestParam：Spring注解，用于从URL查询参数中获取参数
            // 作用：将URL中的token参数值绑定到方法的token参数上，required = false表示可选
            @RequestParam(required = false) String token,
            // @RequestHeader：Spring注解，用于从请求头中获取参数
            // 作用：将Authorization头的值绑定到方法的authorization参数上，required = false表示可选
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从Authorization头中提取token，如果没有则使用URL查询参数中的token
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            
            // 如果没有提供token，返回401状态码和错误信息
            if (token == null || token.isEmpty()) {
                response.put("message", "未提供token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // 调用服务层的方法验证token
            boolean isValid = authService.validateToken(token);
            
            // 返回验证结果和对应的状态码
            if (isValid) {
                response.put("message", "token有效");
                response.put("valid", true);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "token无效");
                response.put("valid", false);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            // 统一处理异常，返回错误信息和401状态码
            response.put("message", "token验证失败");
            response.put("valid", false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
