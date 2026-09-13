package com.example.zcwl.controller;

import com.example.zcwl.entity.DocContents;
import com.example.zcwl.service.DocContentsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 文档内容控制器类
 * <p>
 * 这个类的作用是处理与文档内容相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现文档内容数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

@RestController
@RequestMapping({"/doc-contents"})


public class DocContentsController {

    // 注入文档内容服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final DocContentsService docContentsService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将文档内容服务对象注入到控制器中
     * @param docContentsService 文档内容服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建DocContentsController对象时，自动将DocContentsService的实例传递给这个构造函数
    @Autowired
    public DocContentsController(DocContentsService docContentsService) {
        this.docContentsService = docContentsService;
    }

    /**
     * 创建文档内容
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/doc-contents
     * - 请求体：JSON格式的文档内容数据，例如：
     *   {
     *     "id": {
     *       "docId": 1,
     *       "chapterId": 1
     *     },
     *     "title": "第一章 引言",
     *     "content": "这是第一章的内容..."
     *   }
     * - 响应：创建成功后返回创建的文档内容数据和201状态码
     *
     * @param docContent 文档内容实体，包含文档内容信息
     * @return 包含创建的文档内容实体和HTTP状态码的ResponseEntity对象
     */

    @PostMapping

    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<DocContents> createDocContent(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果docContent对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为DocContents对象
            @RequestBody DocContents docContent) {
        
        // 调用服务层的方法创建文档内容记录
        DocContents createdDocContent = docContentsService.createDocContent(docContent);
        
        // 返回创建成功的文档内容实体和201状态码
        // HttpStatus.CREATED表示资源创建成功
        return new ResponseEntity<>(createdDocContent, HttpStatus.CREATED);
    }

    /**
     * 根据文档ID和章节ID查询文档内容
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-contents/{docId}/{chapterId}
     *   例如：http://localhost:8080/api/doc-contents/1/2
     * - 响应：找到数据返回文档内容数据和200状态码；找不到返回404状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @param chapterId 章节ID，从URL路径中获取
     * @return 包含文档内容实体或错误状态码的ResponseEntity对象
     */

    @GetMapping("/{docId}/{chapterId}")

    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<DocContents> getDocContentById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{chapterId}值绑定到方法的chapterId参数上
            @PathVariable Integer chapterId) {
        
        // 创建复合主键对象，因为文档内容表的主键由文档ID和章节ID组成
        DocContents.DocContentsId id = new DocContents.DocContentsId(docId, chapterId);
        
        // 调用服务层方法，根据复合主键查询文档内容记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<DocContents> optionalDocContent = docContentsService.getDocContentById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalDocContent.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalDocContent为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 根据文档ID查询所有文档内容
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-contents/doc/{docId}
     *   例如：http://localhost:8080/api/doc-contents/doc/1
     * - 响应：返回该文档的所有内容列表和200状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @return 包含文档内容列表和HTTP状态码的ResponseEntity对象
     */

    @GetMapping("/doc/{docId}")

    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<List<DocContents>> getDocContentsByDocId(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId) {
        
        // 调用服务层方法，根据文档ID查询所有文档内容记录
        List<DocContents> docContents = docContentsService.getDocContentsByDocId(docId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(docContents);
    }

    /**
     * 查询所有文档内容（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/doc-contents?page=0&size=10&sort=id.docId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如id.docId,asc表示按文档ID升序排序
     * - 响应：返回分页后的文档内容数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页文档内容列表的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/doc-contents时，会调用这个方法
    @GetMapping("/contents")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Page<DocContents>> getAllDocContents(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有文档内容记录（分页）
        Page<DocContents> docContents = docContentsService.getAllDocContents(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(docContents);
    }

    /**
     * 更新文档内容
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/doc-contents/{docId}/{chapterId}
     *   例如：http://localhost:8080/api/doc-contents/1/2
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "title": "第一章 更新后的标题",
     *     "content": "更新后的内容..."
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @param chapterId 章节ID，从URL路径中获取
     * @param docContent 更新后的文档内容信息
     * @return 包含更新后文档内容实体的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到/api/doc-contents/{docId}/{chapterId}时，会调用这个方法
    @PutMapping("/{docId}/{chapterId}")

    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<DocContents> updateDocContent(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{chapterId}值绑定到方法的chapterId参数上
            @PathVariable Integer chapterId, 
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果docContent对象不符合验证规则，会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为DocContents对象
            @RequestBody DocContents docContent) {
        
        // 创建复合主键对象，因为文档内容表的主键由文档ID和章节ID组成
        DocContents.DocContentsId id = new DocContents.DocContentsId(docId, chapterId);
        
        // 调用服务层方法，更新文档内容记录
        DocContents updatedDocContent = docContentsService.updateDocContent(id, docContent);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(updatedDocContent);
    }

    /**
     * 删除文档内容
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/doc-contents/{docId}/{chapterId}
     *   例如：http://localhost:8080/api/doc-contents/1/2
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param docId 文档ID，从URL路径中获取
     * @param chapterId 章节ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到/api/doc-contents/{docId}/{chapterId}时，会调用这个方法
    @DeleteMapping("/{docId}/{chapterId}")

    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    // Void表示响应体为空
    public ResponseEntity<Void> deleteDocContent(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{docId}值绑定到方法的docId参数上
            @PathVariable Integer docId, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{chapterId}值绑定到方法的chapterId参数上
            @PathVariable Integer chapterId) {
        
        // 创建复合主键对象，因为文档内容表的主键由文档ID和章节ID组成
        DocContents.DocContentsId id = new DocContents.DocContentsId(docId, chapterId);
        
        // 调用服务层方法，删除文档内容记录
        docContentsService.deleteDocContent(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}