package com.example.zcwl.controller;

import com.example.zcwl.entity.User;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.dto.LoginRequestDTO;
import com.example.zcwl.dto.LoginResponseDTO;
import com.example.zcwl.service.UserService;
import com.example.zcwl.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * 用户控制器类
 * <p>
 * 这个类的作用是处理与用户相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现用户数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/users开头
// 例如：http://localhost:8080/users
@RequestMapping("/users")

// @Validated：Spring注解，用于启用方法参数验证
// 作用：配合@Valid注解使用，触发方法参数的验证逻辑
@org.springframework.validation.annotation.Validated

public class UserController {

    // 注入用户服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final UserService userService;
    private final AuthService authService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将用户服务对象和认证服务对象注入到控制器中
     * @param userService 用户服务对象，由Spring容器自动注入
     * @param authService 认证服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建UserController对象时，自动将UserService和AuthService的实例传递给这个构造函数
    @Autowired
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * 用户登录
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/users/login
     * - 请求体：JSON格式的登录数据，例如：
     *   {
     *     "userId": "user001",
     *     "password": "123456"
     *   }
     * - 响应：登录成功返回userName、userId和token，状态码200；登录失败返回401状态码和空对象
     *
     * @param loginRequestDTO 登录请求数据，由@RequestBody注解自动转换
     * @return 包含登录结果的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/users/login时，会调用这个方法
    @PostMapping("/login")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> login(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果loginRequestDTO对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为LoginRequestDTO对象
            @RequestBody LoginRequestDTO loginRequestDTO) {
        System.out.println("Login request received: userId=" + loginRequestDTO.getUserId() + ", password=" + loginRequestDTO.getPassword());
        try {
            System.out.println("Calling authService.login()");
            // 调用服务层的方法进行登录
            LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
            System.out.println("Login successful, returning response");
            // 登录成功，返回用户信息和token
            return ResponseEntity.ok(loginResponseDTO);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            // 登录失败，返回401状态码和空对象
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new java.util.HashMap<>());
        }
    }

    /**
     * 创建用户
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/users
     * - 请求体：JSON格式的用户数据，例如：
     *   {
     *     "uId": "user001",
     *     "name": "张三",
     *     "email": "zhangsan@example.com",
     *     "password": "123456"
     *   }
     * - 响应：创建成功后返回创建的用户数据和201状态码
     *
     * @param userDTO 用户数据传输对象，由@RequestBody注解自动转换
     * @return 包含创建的用户数据和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/users时，会调用这个方法
    @PostMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> createUser(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果userDTO对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为UserDTO对象
            @RequestBody UserDTO userDTO) {
        try {
            // 调用服务层的方法创建用户记录
            User user = userService.createUser(userDTO);
            
            // 返回创建成功的用户数据和201状态码
            // HttpStatus.CREATED表示资源创建成功
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // 处理业务逻辑错误，返回400状态码
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "服务器内部错误");
            response.put("error", e.getMessage());
            response.put("status", 400);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // 处理其他错误，返回500状态码
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "服务器内部错误");
            response.put("error", e.getMessage());
            response.put("status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据用户ID查询单个用户
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/users/{id}
     *   例如：http://localhost:8080/users/user001
     * - 请求头：
     *   - Content-Type: application/json
     *   - Authorization: Bearer token
     * - 响应：找到数据返回用户数据和200状态码；验证失败返回401状态码；找不到返回404状态码
     *
     * @param id 用户ID，从URL路径中获取
     * @return 包含用户数据或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/users/{id}时，会调用这个方法
    // {id}是路径参数，用于传递用户ID
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")

    public ResponseEntity<?> getUserById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable String id) {
        // 调用服务层方法，根据用户ID查询用户记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<User> optionalUser = userService.getUserById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalUser.map(user -> {
            // 构建响应对象，包含 name 和 email
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        })
        .orElseGet(
            // 如果optionalUser为空，调用ResponseEntity.notFound().build()
            // 作用：创建一个包含404状态码的ResponseEntity对象
            () -> ResponseEntity.notFound().build());
    }

    /**
     * 查询所有用户（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/users?page=0&size=10&sort=uId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如uId,asc表示按用户ID升序排序
     * - 响应：返回分页后的用户数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页用户数据的ResponseEntity对象
     */
    @GetMapping

    public ResponseEntity<Page<User>> getAllUsers(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有用户记录（分页）
        Page<User> users = userService.getAllUsers(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(users);
    }

    /**
     * 更新用户信息
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/users/{id}
     *   例如：http://localhost:8080/api/users/user001
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "name": "李四",
     *     "email": "lisi@example.com",
     *     "password": "654321"
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param id 用户ID，从URL路径中获取
     * @param userDTO 更新的用户数据，由@RequestBody注解自动转换
     * @return 包含更新后数据的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到指定URL时，会调用这个方法
    @PutMapping("/{id}")

    public ResponseEntity<User> updateUser(
            // @PathVariable：从URL路径中获取用户ID
            @PathVariable String id, 
            // @Valid：验证请求体数据的合法性
            // @RequestBody：将请求体JSON转换为UserDTO对象
            @Valid @RequestBody UserDTO userDTO) {
        
        // 调用服务层方法，更新用户记录
        User user = userService.updateUser(id, userDTO);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(user);
    }

    /**
     * 删除用户
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/users/{id}
     *   例如：http://localhost:8080/api/users/user001
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param id 用户ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到指定URL时，会调用这个方法
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteUser(
            // @PathVariable：从URL路径中获取用户ID
            @PathVariable String id) {
        
        // 调用服务层方法，删除用户记录
        userService.deleteUser(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新用户信息
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/users/{id}
     *   例如：http://localhost:8080/users/user001
     * - 请求头：
     *   - Content-Type: application/json
     *   - Authorization: Bearer token
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "name": "张三",
     *     "email": "zhangsan@example.com"
     *   }
     * - 响应：更新成功返回200状态码和更新后的用户信息；验证失败返回401状态码；无权限返回403状态码；找不到返回404状态码
     *
     * @param id 用户ID，从URL路径中获取
     * @param requestBody 请求体，包含name和email字段
     * @return 包含更新后用户信息的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/users/{id}时，会调用这个方法
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}")

    public ResponseEntity<?> updateUserInfo(
            // @PathVariable：从URL路径中获取用户ID
            @PathVariable String id,
            // @RequestBody：将请求体JSON转换为Map对象
            @RequestBody java.util.Map<String, String> requestBody) {
        try {
            // 获取当前认证用户
            org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 检查是否有权限修改该用户信息
            if (!currentUserId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 获取请求参数
            String name = requestBody.get("name");
            String email = requestBody.get("email");
            
            // 验证参数
            if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            // 调用服务层方法，更新用户信息
            User user = userService.updateUserInfo(id, name, email);
            
            // 构建响应对象
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            
            // 返回更新后的用户信息和200状态码
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("User not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新用户密码
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/users/password/{id}
     *   例如：http://localhost:8080/users/password/user001
     * - 请求头：
     *   - Content-Type: application/json
     *   - Authorization: Bearer token
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "password": "currentPassword",
     *     "newPassword": "newPassword123!"
     *   }
     * - 响应：更新成功返回200状态码和空密码字段；验证失败返回401状态码；无权限返回403状态码；找不到返回404状态码
     *
     * @param id 用户ID，从URL路径中获取
     * @param requestBody 请求体，包含password和newPassword字段
     * @return 包含响应信息的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/users/password/{id}时，会调用这个方法
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/password/{id}")

    public ResponseEntity<?> updatePassword(
            // @PathVariable：从URL路径中获取用户ID
            @PathVariable String id,
            // @RequestBody：将请求体JSON转换为Map对象
            @RequestBody java.util.Map<String, String> requestBody) {
        try {
            // 获取当前认证用户
            org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 检查是否有权限修改该用户密码
            if (!currentUserId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 获取请求参数
            String oldPassword = requestBody.get("password");
            String newPassword = requestBody.get("newPassword");
            
            // 验证参数
            if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            // 调用服务层方法，更新密码
            userService.updatePassword(id, oldPassword, newPassword);
            
            // 构建响应对象
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("password", "");
            
            // 返回成功响应和200状态码
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("User not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().equals("原密码错误，请重新输入")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}