package com.example.zcwl.controller;

import com.example.zcwl.dto.LoginRequestDTO;
import com.example.zcwl.dto.LoginResponseDTO;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.entity.User;
import com.example.zcwl.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    /**
     * 用户登录
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/
     * - 请求体：JSON格式的登录数据，例如：
     *   {
     *     "userId": "1",
     *     "password": "123456"
     *   }
     * - 响应：登录成功返回username、userId和token，状态码200；登录失败返回错误信息，状态码401
     *
     * @param loginRequestDTO 登录请求数据，由@RequestBody注解自动转换
     * @return 包含登录结果的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/时，会调用这个方法
    @PostMapping("")
    

    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> login(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果loginRequestDTO对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为LoginRequestDTO对象
            @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            // 调用服务层的方法进行登录
            LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
            
            // 返回登录成功的响应数据和200状态码
            return new ResponseEntity<>(loginResponseDTO, HttpStatus.OK);
        } catch (Exception e) {
            // 登录失败，返回401状态码和错误信息
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 用户注册
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/auth/register
     * - 请求体：JSON格式的注册数据，例如：
     *   {
     *     "username": "张三",
     *     "password": "123456",
     *     "email": "zhangsan@example.com",
     *     "phone": "13800138000"
     *   }
     * - 响应：注册成功返回用户信息，状态码201；注册失败返回错误信息，状态码400
     *
     * @param userDTO 用户注册数据，由@RequestBody注解自动转换
     * @return 包含注册结果的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/auth/register时，会调用这个方法
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
        } catch (Exception e) {
            // 注册失败，返回400状态码和错误信息
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 验证token
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/auth/validate-token?token={token}
     *   例如：http://localhost:8080/api/auth/validate-token?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * - 响应：token有效返回true，状态码200；token无效返回false，状态码401
     *
     * @param token JWT token，从URL查询参数中获取
     * @return 包含验证结果的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/auth/validate-token时，会调用这个方法
    @GetMapping("/validate-token")
    

    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Boolean> validateToken(
            // @RequestParam：Spring注解，用于从URL查询参数中获取参数
            // 作用：将URL中的token参数值绑定到方法的token参数上
            @RequestParam String token) {
        
        // 调用服务层的方法验证token
        boolean isValid = authService.validateToken(token);
        
        // 返回验证结果和对应的状态码
        if (isValid) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        }
    }
}
