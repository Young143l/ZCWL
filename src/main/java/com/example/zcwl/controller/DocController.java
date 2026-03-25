package com.example.zcwl.controller;

import com.example.zcwl.entity.Doc;
import com.example.zcwl.entity.DocContents;
import com.example.zcwl.entity.DocComment;
import com.example.zcwl.service.DocService;
import com.example.zcwl.service.DocContentsService;
import com.example.zcwl.service.DocCommentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

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
// 作用：所有该控制器下的API接口都会以/doc开头
// 例如：http://localhost:8080/doc
@RequestMapping("/doc")


public class DocController {

    // 注入文档服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final DocService docService;
    private final DocContentsService docContentsService;
    private final DocCommentService docCommentService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将文档服务对象和文档内容服务对象注入到控制器中
     * @param docService 文档服务对象，由Spring容器自动注入
     * @param docContentsService 文档内容服务对象，由Spring容器自动注入
     * @param docCommentService 文档评论服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建DocController对象时，自动将DocService和DocContentsService的实例传递给这个构造函数
    @Autowired
    public DocController(DocService docService, DocContentsService docContentsService, DocCommentService docCommentService) {
        this.docService = docService;
        this.docContentsService = docContentsService;
        this.docCommentService = docCommentService;
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
     * 根据ID查询文档详情
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/doc/{d_id}
     *   例如：http://localhost:8080/doc/1
     * - 请求头：Content-Type: application/json
     * - 响应：找到数据返回包含docInfo和docDir的响应，状态码200；找不到返回404状态码
     *
     * @param d_id 文档ID，从URL路径中获取
     * @return 包含文档详情和目录的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/doc/{d_id}时，会调用这个方法
    // {d_id}是路径参数，用于传递文档ID
    @GetMapping("/{d_id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> getDocById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{d_id}值绑定到方法的d_id参数上
            @PathVariable Integer d_id) {
        // 调用服务层方法，根据文档ID查询文档记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<Doc> optionalDoc = docService.getDocById(d_id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalDoc.map(doc -> {
            // 构建响应对象
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            
            // 构建docInfo对象
            java.util.Map<String, Object> docInfo = new java.util.HashMap<>();
            docInfo.put("id", doc.getDocId());
            docInfo.put("name", doc.getDocName());
            docInfo.put("summary", doc.getSummary());
            docInfo.put("img", doc.getIcon());
            
            // 构建docDir对象（文档目录）
            java.util.List<java.util.Map<String, Object>> docDir = new java.util.ArrayList<>();
            // 查询该文档的所有目录
            java.util.List<DocContents> contents = docContentsService.getDocContentsByDocId(doc.getDocId());
            for (DocContents content : contents) {
                java.util.Map<String, Object> dirItem = new java.util.HashMap<>();
                dirItem.put("id", content.getId().getChapterId());
                dirItem.put("name", content.getName());
                docDir.add(dirItem);
            }
            
            response.put("docInfo", docInfo);
            response.put("docDir", docDir);
            
            return ResponseEntity.ok(response);
        })
        .orElseGet(
            // 如果optionalDoc为空，调用ResponseEntity.notFound().build()
            // 作用：创建一个包含404状态码的ResponseEntity对象
            () -> ResponseEntity.notFound().build());
    }

    /**
     * 查询所有文档
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/doc
     * - 请求头：Content-Type: application/json
     * - 响应：返回包含docList的响应，每个文档包含id、name、summary和img，状态码200；失败返回404状态码
     *
     * @return 包含文档列表的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/doc时，会调用这个方法
    @GetMapping
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> getAllDocs() {
        try {
            // 调用服务层方法，获取所有文档记录
            java.util.List<Doc> docs = docService.getAllDocs();
            
            // 构建响应对象
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            List<Map<String, Object>> docList = getMaps(docs);

            response.put("docList", docList);
            
            // 返回文档列表和200状态码
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 失败时返回404状态码
            return ResponseEntity.notFound().build();
        }
    }

    private static List<Map<String, Object>> getMaps(List<Doc> docs) {
        List<Map<String, Object>> docList = new java.util.ArrayList<>();

        // 转换文档数据格式
        for (Doc doc : docs) {
            Map<String, Object> docItem = new java.util.HashMap<>();
            docItem.put("id", doc.getDocId());
            docItem.put("name", doc.getDocName());
            docItem.put("summary", doc.getSummary());
            docItem.put("img", doc.getIcon());
            docList.add(docItem);
        }
        return docList;
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

    /**
     * 获取文档内容
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/doc/{d_id}/{c_id}
     *   例如：http://localhost:8080/doc/1/2
     * - 请求头：Content-Type: application/json
     * - 响应：找到数据返回包含d_id、c_id、title和content的响应，状态码200；找不到返回404状态码
     *
     * @param d_id 文档ID，从URL路径中获取
     * @param c_id 章节ID，从URL路径中获取
     * @return 包含文档内容的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/doc/{d_id}/{c_id}时，会调用这个方法
    @GetMapping("/{d_id}/{c_id}")
    
    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<?> getDocContent(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{d_id}值绑定到方法的d_id参数上
            @PathVariable Integer d_id, 
            
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{c_id}值绑定到方法的c_id参数上
            @PathVariable Integer c_id) {
        try {
            // 创建复合主键对象，因为文档内容表的主键由文档ID和章节ID组成
            DocContents.DocContentsId id = new DocContents.DocContentsId(d_id, c_id);
            
            // 调用服务层方法，根据复合主键查询文档内容记录
            Optional<DocContents> optionalDocContent = docContentsService.getDocContentById(id);
            
            // 使用Optional的map方法处理查询结果
            // 如果找到数据，返回200状态码和数据；否则返回404状态码
            return optionalDocContent.map(docContent -> {
                // 构建响应对象
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("d_id", d_id);
                response.put("c_id", c_id);
                response.put("title", docContent.getName());
                response.put("content", docContent.getContent());
                return ResponseEntity.ok(response);
            })
            .orElseGet(
                // 如果optionalDocContent为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            // 失败时返回404状态码
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取指定文档章节的评论
     * 接口：GET /doc/comment/:d_id/:c_id
     * @param dId 文档ID
     * @param cId 章节ID
     * @return 评论列表
     */
    @GetMapping("/comment/{dId}/{cId}")
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable Long dId,
            @PathVariable Long cId) {
        try {
            List<Map<String, Object>> comments = docCommentService.getComments(dId, cId);
            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取评论失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 提交评论
     * 接口：POST /doc/comment/:d_id/:c_id
     * @param dId 文档ID
     * @param cId 章节ID
     * @param request 请求参数
     * @param authentication 认证信息
     * @return 评论ID
     */
    @PostMapping("/comment/{dId}/{cId}")
    public ResponseEntity<Map<String, Object>> submitComment(
            @PathVariable Long dId,
            @PathVariable Long cId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            // 从token中获取当前用户ID
            String userId = authentication.getName();
            
            // 验证请求参数
            if (!request.containsKey("email") || !request.containsKey("content")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "email和content是必填项");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 构建评论对象
            DocComment comment = new DocComment();
            comment.setDId(dId);
            comment.setCId(cId);
            comment.setUId(userId);  // 使用token中的用户ID
            comment.setEmail((String) request.get("email"));
            comment.setComment((String) request.get("content"));
            
            // 设置父评论ID，默认为-1（一级评论）
            Long fa = request.containsKey("fa") ? Long.parseLong(request.get("fa").toString()) : -1;
            comment.setFa(fa);

            // 保存评论
            DocComment savedComment = docCommentService.saveComment(comment);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedComment.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "提交评论失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}