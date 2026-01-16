package com.example.zcwl.controller;

import com.example.zcwl.entity.QueAns;
import com.example.zcwl.service.QueAnsService;

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
 * 问答控制器类
 * <p>
 * 这个类的作用是处理与问答相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现问答数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/api/que-ans开头
// 例如：http://localhost:8080/api/que-ans
@RequestMapping("/api/que-ans")


public class QueAnsController {

    // 注入问答服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final QueAnsService queAnsService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将问答服务对象注入到控制器中
     * @param queAnsService 问答服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建QueAnsController对象时，自动将QueAnsService的实例传递给这个构造函数
    @Autowired
    public QueAnsController(QueAnsService queAnsService) {
        this.queAnsService = queAnsService;
    }

    /**
     * 创建问答记录
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/que-ans
     * - 请求体：JSON格式的问答数据，例如：
     *   {
     *     "id": {
     *       "dId": 1,
     *       "times": 1
     *     },
     *     "que": "什么是Spring Boot？",
     *     "ans": "Spring Boot是一个用于快速开发Spring应用的框架",
     *     "date": "2024-01-15T10:00:00"
     *   }
     * - 响应：创建成功后返回创建的问答数据和201状态码
     *
     * @param queAns 前端传递的问答数据，由@RequestBody注解自动转换为QueAns对象
     * @return 包含创建的问答数据和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/que-ans时，会调用这个方法
    @PostMapping

    
    
    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<QueAns> createQueAns(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果queAns对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid 
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为QueAns对象
            @RequestBody QueAns queAns) {
        
        // 调用服务层的方法创建问答记录
        QueAns createdQueAns = queAnsService.createQueAns(queAns);
        
        // 返回创建成功的问答数据和201状态码
        // HttpStatus.CREATED表示资源创建成功
        return new ResponseEntity<>(createdQueAns, HttpStatus.CREATED);
    }

    /**
     * 根据对话ID和问答次数查询单个问答记录
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/que-ans/{dialogId}/{times}
     *   例如：http://localhost:8080/api/que-ans/1/2
     * - 响应：找到数据返回问答数据和200状态码；找不到返回404状态码
     *
     * @param dialogId 对话ID，从URL路径中获取
     * @param times 问答次数，从URL路径中获取
     * @return 包含问答数据或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/que-ans/{dialogId}/{times}时，会调用这个方法
    // {dialogId}和{times}是路径参数，用于传递对话ID和问答次数
    @GetMapping("/{dialogId}/{times}")

    public ResponseEntity<QueAns> getQueAnsById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{dialogId}值绑定到方法的dialogId参数上
            @PathVariable Integer dialogId, 
            @PathVariable Integer times) {
        
        // 创建复合主键对象，因为问答表的主键由对话ID和问答次数组成
        QueAns.QueAnsId id = new QueAns.QueAnsId(dialogId, times);
        
        // 调用服务层方法，根据复合主键查询问答记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<QueAns> optionalQueAns = queAnsService.getQueAnsById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalQueAns.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalQueAns为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 根据对话ID查询该对话下的所有问答记录
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/que-ans/dialog/{dialogId}
     *   例如：http://localhost:8080/api/que-ans/dialog/1
     * - 响应：返回该对话下的所有问答数据列表和200状态码
     *
     * @param dialogId 对话ID，从URL路径中获取
     * @return 包含问答列表数据的ResponseEntity对象
     */
    @GetMapping("/dialog/{dialogId}")

    public ResponseEntity<List<QueAns>> getQueAnsByDialogId(
            @PathVariable Integer dialogId) {
        
        // 调用服务层方法，根据对话ID查询所有问答记录
        List<QueAns> queAnsList = queAnsService.getQueAnsByDialogId(dialogId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(queAnsList);
    }

    /**
     * 查询所有问答记录（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/que-ans?page=0&size=10&sort=id.dId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如id.dId,asc表示按对话ID升序排序
     * - 响应：返回分页后的问答数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页问答数据的ResponseEntity对象
     */
    @GetMapping

    public ResponseEntity<Page<QueAns>> getAllQueAns(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            Pageable pageable) {
        
        // 调用服务层方法，获取所有问答记录（分页）
        Page<QueAns> queAnsList = queAnsService.getAllQueAns(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(queAnsList);
    }

    /**
     * 更新问答记录
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/que-ans/{dialogId}/{times}
     *   例如：http://localhost:8080/api/que-ans/1/2
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "que": "更新后的问题？",
     *     "ans": "更新后的回答"
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param dialogId 对话ID，从URL路径中获取
     * @param times 问答次数，从URL路径中获取
     * @param queAns 更新的问答数据，由@RequestBody注解自动转换
     * @return 包含更新后数据的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到指定URL时，会调用这个方法
    @PutMapping("/{dialogId}/{times}")

    public ResponseEntity<QueAns> updateQueAns(
            @PathVariable Integer dialogId, 
            @PathVariable Integer times, 
            @Valid @RequestBody QueAns queAns) {
        
        // 创建复合主键对象
        QueAns.QueAnsId id = new QueAns.QueAnsId(dialogId, times);
        
        // 调用服务层方法，更新问答记录
        QueAns updatedQueAns = queAnsService.updateQueAns(id, queAns);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(updatedQueAns);
    }

    /**
     * 删除问答记录
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/que-ans/{dialogId}/{times}
     *   例如：http://localhost:8080/api/que-ans/1/2
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param dialogId 对话ID，从URL路径中获取
     * @param times 问答次数，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到指定URL时，会调用这个方法
    @DeleteMapping("/{dialogId}/{times}")

    public ResponseEntity<Void> deleteQueAns(
            @PathVariable Integer dialogId, 
            @PathVariable Integer times) {
        
        // 创建复合主键对象
        QueAns.QueAnsId id = new QueAns.QueAnsId(dialogId, times);
        
        // 调用服务层方法，删除问答记录
        queAnsService.deleteQueAns(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}