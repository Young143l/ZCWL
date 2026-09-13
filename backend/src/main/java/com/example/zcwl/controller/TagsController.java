package com.example.zcwl.controller;

import com.example.zcwl.entity.Tags;
import com.example.zcwl.service.TagsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * 标签控制器类
 * <p>
 * 这个类的作用是处理与标签相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现标签数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/tags开头
// 例如：http://localhost:8080/tags
@RequestMapping("/tags")

@org.springframework.validation.annotation.Validated
public class TagsController {

    // 注入标签服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final TagsService tagsService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将标签服务对象注入到控制器中
     * @param tagsService 标签服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建TagsController对象时，自动将TagsService的实例传递给这个构造函数
    @Autowired
    public TagsController(TagsService tagsService) {
        this.tagsService = tagsService;
    }

    /**
     * 创建标签
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/tags
     * - 请求体：JSON格式的标签数据，例如：
     *   {
     *     "name": "Spring Boot",
     *     "description": "Spring Boot相关标签"
     *   }
     * - 响应：创建成功后返回创建的标签数据和201状态码
     *
     * @param tags 标签实体，包含标签信息
     * @return 包含创建的标签实体和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/tags时，会调用这个方法
    @PostMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> createTag(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果tags对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为Tags对象
            @RequestBody Tags tags) {
        try {
            // 调用服务层的方法创建标签记录
            Tags createdTag = tagsService.createTag(tags);
            
            // 返回创建成功的标签实体和201状态码
            // HttpStatus.CREATED表示资源创建成功
            return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // 捕获运行时异常，返回400状态码和错误信息
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据ID查询标签
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/tags/{id}
     *   例如：http://localhost:8080/api/tags/1
     * - 响应：找到数据返回标签数据和200状态码；找不到返回404状态码
     *
     * @param id 标签ID，从URL路径中获取
     * @return 包含标签实体或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/tags/{id}时，会调用这个方法
    // {id}是路径参数，用于传递标签ID
    @GetMapping("/{id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Tags> getTagById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable Integer id) {
        
        // 调用服务层方法，根据标签ID查询标签记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<Tags> optionalTag = tagsService.getTagById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalTag.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalTag为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 查询所有标签（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/tags?page=0&size=10&sort=tagId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如tagId,asc表示按标签ID升序排序
     * - 响应：返回分页后的标签数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页标签列表的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/tags时，会调用这个方法
    @GetMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Page<Tags>> getAllTags(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有标签记录（分页）
        Page<Tags> tags = tagsService.getAllTags(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(tags);
    }

    /**
     * 更新标签
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/tags/{id}
     *   例如：http://localhost:8080/api/tags/1
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "name": "Spring Boot 2.7",
     *     "description": "Spring Boot 2.7相关标签"
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param id 标签ID，从URL路径中获取
     * @param tags 更新后的标签信息
     * @return 包含更新后标签实体的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到/api/tags/{id}时，会调用这个方法
    @PutMapping("/{id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Tags> updateTag(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable Integer id, 
            
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果tags对象不符合验证规则，会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为Tags对象
            @RequestBody Tags tags) {
        
        // 调用服务层方法，更新标签记录
        Tags updatedTag = tagsService.updateTag(id, tags);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(updatedTag);
    }

    /**
     * 删除标签
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/tags/{id}
     *   例如：http://localhost:8080/api/tags/1
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param id 标签ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到/api/tags/{id}时，会调用这个方法
    @DeleteMapping("/{id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    // Void表示响应体为空
    public ResponseEntity<Void> deleteTag(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable Integer id) {
        
        // 调用服务层方法，删除标签记录
        tagsService.deleteTag(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}