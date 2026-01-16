package com.example.zcwl.controller;

import com.example.zcwl.entity.Doc;
import com.example.zcwl.service.DocService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

/**
 * 文档控制器类
 * <p>
 * 这个类的作用是处理与文档相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现文档数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/api/docs开头
// 例如：http://localhost:8080/api/docs
@RequestMapping("/api/docs")


public class DocController {

    // 注入文档服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final DocService docService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将文档服务对象注入到控制器中
     * @param docService 文档服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建DocController对象时，自动将DocService的实例传递给这个构造函数
    @Autowired
    public DocController(DocService docService) {
        this.docService = docService;
    }

    /**
     * 创建文档
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/docs
     * - 请求体：JSON格式的文档数据，例如：
     *   {
     *     "title": "Spring Boot入门指南",
     *     "author": "张三",
     *     "content": "这是一本关于Spring Boot的入门指南..."
     *   }
     * - 响应：创建成功后返回创建的文档数据和201状态码
     *
     * @param doc 文档实体，包含文档信息
     * @return 包含创建的文档实体和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/docs时，会调用这个方法
    @PostMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Doc> createDoc(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果doc对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为Doc对象
            @RequestBody Doc doc) {
        
        // 调用服务层的方法创建文档记录
        Doc createdDoc = docService.createDoc(doc);
        
        // 返回创建成功的文档实体和201状态码
        // HttpStatus.CREATED表示资源创建成功
        return new ResponseEntity<>(createdDoc, HttpStatus.CREATED);
    }

    /**
     * 根据ID查询文档
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/docs/{id}
     *   例如：http://localhost:8080/api/docs/1
     * - 响应：找到数据返回文档数据和200状态码；找不到返回404状态码
     *
     * @param id 文档ID，从URL路径中获取
     * @return 包含文档实体或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/docs/{id}时，会调用这个方法
    // {id}是路径参数，用于传递文档ID
    @GetMapping("/{id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Doc> getDocById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable Integer id) {
        
        // 调用服务层方法，根据文档ID查询文档记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<Doc> optionalDoc = docService.getDocById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalDoc.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalDoc为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 查询所有文档（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/docs?page=0&size=10&sort=dId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如dId,asc表示按文档ID升序排序
     * - 响应：返回分页后的文档数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页文档列表的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/docs时，会调用这个方法
    @GetMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Page<Doc>> getAllDocs(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有文档记录（分页）
        Page<Doc> docs = docService.getAllDocs(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(docs);
    }

    /**
     * 更新文档
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/docs/{id}
     *   例如：http://localhost:8080/api/docs/1
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "title": "Spring Boot进阶指南",
     *     "author": "李四",
     *     "content": "这是一本关于Spring Boot的进阶指南..."
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param id 文档ID，从URL路径中获取
     * @param doc 更新后的文档信息
     * @return 包含更新后文档实体的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到/api/docs/{id}时，会调用这个方法
    @PutMapping("/{id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Doc> updateDoc(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable Integer id, 
            
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果doc对象不符合验证规则，会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为Doc对象
            @RequestBody Doc doc) {
        
        // 调用服务层方法，更新文档记录
        Doc updatedDoc = docService.updateDoc(id, doc);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(updatedDoc);
    }

    /**
     * 删除文档
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/docs/{id}
     *   例如：http://localhost:8080/api/docs/1
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param id 文档ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到/api/docs/{id}时，会调用这个方法
    @DeleteMapping("/{id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    // Void表示响应体为空
    public ResponseEntity<Void> deleteDoc(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @PathVariable Integer id) {
        
        // 调用服务层方法，删除文档记录
        docService.deleteDoc(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}