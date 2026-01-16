package com.example.zcwl.controller;

import com.example.zcwl.entity.DocTags;
import com.example.zcwl.service.DocTagsService;

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
 * 文档标签关系控制器类
 * <p>
 * 这个类的作用是处理与文档标签关系相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现文档标签关系数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/api/doc-tags开头
// 例如：http://localhost:8080/api/doc-tags
@RequestMapping("/api/doc-tags")


public class DocTagsController {

    // 注入文档标签关系服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final DocTagsService docTagsService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将文档标签关系服务对象注入到控制器中
     * @param docTagsService 文档标签关系服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建DocTagsController对象时，自动将DocTagsService的实例传递给这个构造函数
    @Autowired
    public DocTagsController(DocTagsService docTagsService) {
        this.docTagsService = docTagsService;
    }

    /**
     * 创建文档标签关系
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/doc-tags
     * - 请求体：JSON格式的文档标签关系数据，例如：
     *   {
     *     "id": {
     *       "docId": 1,
     *       "tagId": 1
     *     }
     *   }
     * - 响应：创建成功后返回创建的文档标签关系数据和201状态码
     *
     * @param docTag 文档标签关系实体，包含文档标签关系信息
     * @return 包含创建的文档标签关系实体和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/doc-tags时，会调用这个方法
    @PostMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<DocTags> createDocTag(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果docTag对象不符合验证规则，会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为DocTags对象
            @RequestBody DocTags docTag) {
        
        // 调用服务层的方法创建文档标签关系记录
        DocTags createdDocTag = docTagsService.createDocTag(docTag);
        
        // 返回创建成功的文档标签关系实体和201状态码
        // HttpStatus.CREATED表示资源创建成功
        return new ResponseEntity<>(createdDocTag, HttpStatus.CREATED);
    }

    /**
     * 根据文档ID和标签ID查询文档标签关系
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-tags/{docId}/{tagId}
     *   例如：http://localhost:8080/api/doc-tags/1/2
     * - 响应：找到数据返回文档标签关系数据和200状态码；找不到返回404状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @param tagId 标签ID，从URL路径中获取
     * @return 包含文档标签关系实体或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/doc-tags/{docId}/{tagId}时，会调用这个方法
    // {docId}和{tagId}是路径参数，用于传递文档ID和标签ID
    @GetMapping("/{docId}/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<DocTags> getDocTagById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId) {
        
        // 创建复合主键对象，因为文档标签关系表的主键由文档ID和标签ID组成
        DocTags.DocTagsId id = new DocTags.DocTagsId(docId, tagId);
        
        // 调用服务层方法，根据复合主键查询文档标签关系记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<DocTags> optionalDocTag = docTagsService.getDocTagById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalDocTag.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalDocTag为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 根据文档ID查询所有文档标签关系
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-tags/doc/{docId}
     *   例如：http://localhost:8080/api/doc-tags/doc/1
     * - 响应：返回该文档的所有标签关系列表和200状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @return 包含文档标签关系列表和HTTP状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/doc-tags/doc/{docId}时，会调用这个方法
    // {docId}是路径参数，用于传递文档ID
    @GetMapping("/doc/{docId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<List<DocTags>> getDocTagsByDocId(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId) {
        
        // 调用服务层方法，根据文档ID查询所有文档标签关系记录
        List<DocTags> docTags = docTagsService.getDocTagsByDocId(docId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(docTags);
    }

    /**
     * 根据标签ID查询所有文档标签关系
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-tags/tag/{tagId}
     *   例如：http://localhost:8080/api/doc-tags/tag/1
     * - 响应：返回该标签的所有文档关系列表和200状态码
     *
     * @param tagId 标签ID，从URL路径中获取
     * @return 包含文档标签关系列表和HTTP状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/doc-tags/tag/{tagId}时，会调用这个方法
    // {tagId}是路径参数，用于传递标签ID
    @GetMapping("/tag/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<List<DocTags>> getDocTagsByTagId(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId) {
        
        // 调用服务层方法，根据标签ID查询所有文档标签关系记录
        List<DocTags> docTags = docTagsService.getDocTagsByTagId(tagId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(docTags);
    }

    /**
     * 查询所有文档标签关系（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-tags?page=0&size=10&sort=id.docId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如id.docId,asc表示按文档ID升序排序
     * - 响应：返回分页后的文档标签关系数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页文档标签关系列表的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/doc-tags时，会调用这个方法
    @GetMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Page<DocTags>> getAllDocTags(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有文档标签关系记录（分页）
        Page<DocTags> docTags = docTagsService.getAllDocTags(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(docTags);
    }

    /**
     * 删除文档标签关系
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/doc-tags/{docId}/{tagId}
     *   例如：http://localhost:8080/api/doc-tags/1/2
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @param tagId 标签ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到/api/doc-tags/{docId}/{tagId}时，会调用这个方法
    @DeleteMapping("/{docId}/{tagId}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    // Void表示响应体为空
    public ResponseEntity<Void> deleteDocTag(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{tagId}值绑定到方法的tagId参数上
            @PathVariable Integer tagId) {
        
        // 创建复合主键对象，因为文档标签关系表的主键由文档ID和标签ID组成
        DocTags.DocTagsId id = new DocTags.DocTagsId(docId, tagId);
        
        // 调用服务层方法，删除文档标签关系记录
        docTagsService.deleteDocTag(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}