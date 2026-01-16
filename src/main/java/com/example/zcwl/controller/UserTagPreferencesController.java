package com.example.zcwl.controller;

import com.example.zcwl.entity.UserTagPreferences;
import com.example.zcwl.service.UserTagPreferencesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 用户标签偏好控制器类
 * <p>
 * 这个类的作用是处理与用户标签偏好相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现用户标签偏好数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/api/user-tag-preferences开头
// 例如：http://localhost:8080/api/user-tag-preferences
@RequestMapping("/api/user-tag-preferences")


public class UserTagPreferencesController {

    // 注入用户标签偏好服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final UserTagPreferencesService userTagPreferencesService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将用户标签偏好服务对象注入到控制器中
     * @param userTagPreferencesService 用户标签偏好服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建UserTagPreferencesController对象时，自动将UserTagPreferencesService的实例传递给这个构造函数
    @Autowired
    public UserTagPreferencesController(UserTagPreferencesService userTagPreferencesService) {
        this.userTagPreferencesService = userTagPreferencesService;
    }

    /**
     * 创建用户标签偏好
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/user-tag-preferences
     * - 请求体：JSON格式的用户标签偏好数据，例如：
     *   {
     *     "id": {
     *       "userId": "user001",
     *       "tagId": 1
     *     },
     *     "score": 5.5
     *   }
     * - 响应：创建成功后返回创建的用户标签偏好数据和201状态码
     *
     * @param userTagPreference 用户标签偏好实体，包含用户ID、标签ID和偏好分数
     * @return 包含创建的用户标签偏好实体和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/user-tag-preferences时，会调用这个方法
    @PostMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<UserTagPreferences> createUserTagPreference(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果userTagPreference对象不符合验证规则，会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为UserTagPreferences对象
            @RequestBody UserTagPreferences userTagPreference) {
        
        // 调用服务层的方法创建用户标签偏好记录
        UserTagPreferences createdUserTagPreference = userTagPreferencesService.createUserTagPreference(userTagPreference);
        
        // 返回创建成功的用户标签偏好实体和201状态码
        // HttpStatus.CREATED表示资源创建成功
        return new ResponseEntity<>(createdUserTagPreference, HttpStatus.CREATED);
    }

    /**
     * 根据用户ID和标签ID查询用户标签偏好
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/user-tag-preferences/{userId}/{tagId}
     *   例如：http://localhost:8080/api/user-tag-preferences/user001/2
     * - 响应：找到数据返回用户标签偏好数据和200状态码；找不到返回404状态码
     *
     * @param userId 用户ID，从URL路径中获取
     * @param tagId 标签ID，从URL路径中获取
     * @return 包含用户标签偏好实体或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/user-tag-preferences/{userId}/{tagId}时，会调用这个方法
    // {userId}和{tagId}是路径参数，用于传递用户ID和标签ID
    @GetMapping("/{userId}/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<UserTagPreferences> getUserTagPreferenceById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{userId}值绑定到方法的userId参数上
            @PathVariable String userId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId) {
        
        // 创建复合主键对象，因为用户标签偏好表的主键由用户ID和标签ID组成
        UserTagPreferences.UserTagPreferencesId id = new UserTagPreferences.UserTagPreferencesId(userId, tagId);
        
        // 调用服务层方法，根据复合主键查询用户标签偏好记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<UserTagPreferences> optionalUserTagPreference = userTagPreferencesService.getUserTagPreferenceById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalUserTagPreference.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalUserTagPreference为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 根据用户ID查询所有用户标签偏好
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/user-tag-preferences/user/{userId}
     *   例如：http://localhost:8080/api/user-tag-preferences/user/user001
     * - 响应：返回该用户对所有标签的偏好列表和200状态码
     *
     * @param userId 用户ID，从URL路径中获取
     * @return 包含用户标签偏好列表和HTTP状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/user-tag-preferences/user/{userId}时，会调用这个方法
    // {userId}是路径参数，用于传递用户ID
    @GetMapping("/user/{userId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<List<UserTagPreferences>> getUserTagPreferencesByUserId(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{userId}值绑定到方法的userId参数上
            @PathVariable String userId) {
        
        // 调用服务层方法，根据用户ID查询所有用户标签偏好记录
        List<UserTagPreferences> userTagPreferences = userTagPreferencesService.getUserTagPreferencesByUserId(userId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(userTagPreferences);
    }

    /**
     * 根据标签ID查询所有用户标签偏好
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/user-tag-preferences/tag/{tagId}
     *   例如：http://localhost:8080/api/user-tag-preferences/tag/1
     * - 响应：返回所有用户对该标签的偏好列表和200状态码
     *
     * @param tagId 标签ID，从URL路径中获取
     * @return 包含用户标签偏好列表和HTTP状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/user-tag-preferences/tag/{tagId}时，会调用这个方法
    // {tagId}是路径参数，用于传递标签ID
    @GetMapping("/tag/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<List<UserTagPreferences>> getUserTagPreferencesByTagId(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId) {
        
        // 调用服务层方法，根据标签ID查询所有用户标签偏好记录
        List<UserTagPreferences> userTagPreferences = userTagPreferencesService.getUserTagPreferencesByTagId(tagId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(userTagPreferences);
    }

    /**
     * 查询所有用户标签偏好（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/user-tag-preferences?page=0&size=10&sort=id.userId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如id.userId,asc表示按用户ID升序排序
     * - 响应：返回分页后的用户标签偏好数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页用户标签偏好列表的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/user-tag-preferences时，会调用这个方法
    @GetMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Page<UserTagPreferences>> getAllUserTagPreferences(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有用户标签偏好记录（分页）
        Page<UserTagPreferences> userTagPreferences = userTagPreferencesService.getAllUserTagPreferences(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(userTagPreferences);
    }

    /**
     * 更新用户标签偏好
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/user-tag-preferences/{userId}/{tagId}
     *   例如：http://localhost:8080/api/user-tag-preferences/user001/2
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "score": 7.8
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param userId 用户ID，从URL路径中获取
     * @param tagId 标签ID，从URL路径中获取
     * @param userTagPreference 更新后的用户标签偏好信息
     * @return 包含更新后用户标签偏好实体的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到/api/user-tag-preferences/{userId}/{tagId}时，会调用这个方法
    @PutMapping("/{userId}/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<UserTagPreferences> updateUserTagPreference(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{userId}值绑定到方法的userId参数上
            @PathVariable String userId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId, 
            
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果userTagPreference对象不符合验证规则，会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为UserTagPreferences对象
            @RequestBody UserTagPreferences userTagPreference) {
        
        // 创建复合主键对象，因为用户标签偏好表的主键由用户ID和标签ID组成
        UserTagPreferences.UserTagPreferencesId id = new UserTagPreferences.UserTagPreferencesId(userId, tagId);
        
        // 调用服务层方法，更新用户标签偏好记录
        UserTagPreferences updatedUserTagPreference = userTagPreferencesService.updateUserTagPreference(id, userTagPreference);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(updatedUserTagPreference);
    }

    /**
     * 删除用户标签偏好
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/user-tag-preferences/{userId}/{tagId}
     *   例如：http://localhost:8080/api/user-tag-preferences/user001/2
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param userId 用户ID，从URL路径中获取
     * @param tagId 标签ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到/api/user-tag-preferences/{userId}/{tagId}时，会调用这个方法
    @DeleteMapping("/{userId}/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    // Void表示响应体为空
    public ResponseEntity<Void> deleteUserTagPreference(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{userId}值绑定到方法的userId参数上
            @PathVariable String userId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId) {
        
        // 创建复合主键对象，因为用户标签偏好表的主键由用户ID和标签ID组成
        UserTagPreferences.UserTagPreferencesId id = new UserTagPreferences.UserTagPreferencesId(userId, tagId);
        
        // 调用服务层方法，删除用户标签偏好记录
        userTagPreferencesService.deleteUserTagPreference(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}