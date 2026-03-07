package com.example.zcwl.controller;

import com.example.zcwl.entity.Dialog;
import com.example.zcwl.service.DialogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * 对话控制器类
 * <p>
 * 这个类的作用是处理与对话相关的所有HTTP请求，
 * 提供RESTful API接口给前端调用，
 * 实现对话数据的增删改查功能。
 * <p>
 * 前端通过发送不同的HTTP请求（GET/POST/PUT/DELETE）
 * 到对应的URL路径，与后端进行数据交互。
 */

// @RestController：Spring注解，声明这是一个REST风格的控制器
// 作用：自动将方法的返回值转换为JSON格式返回给前端
@RestController

// @RequestMapping：设置这个控制器处理的基础URL路径
// 作用：所有该控制器下的API接口都会以/dialogs开头
// 例如：http://localhost:8080/dialogs
@RequestMapping("/dialogs")

@org.springframework.validation.annotation.Validated

public class DialogController {

    // 注入对话服务对象，用于调用业务逻辑
    // 注意：这里使用final关键字和构造函数注入，是Spring推荐的依赖注入方式
    private final DialogService dialogService;

    /**
     * 构造函数
     * <p>
     * 这个构造函数用于将对话服务对象注入到控制器中
     * @param dialogService 对话服务对象，由Spring容器自动注入
     */
    // @Autowired：Spring注解，用于自动注入依赖
    // 作用：告诉Spring容器，当创建DialogController对象时，自动将DialogService的实例传递给这个构造函数
    @Autowired
    public DialogController(DialogService dialogService) {
        this.dialogService = dialogService;
    }

    /**
     * 创建对话
     * <p>
     * 前端调用方式：
     * - 请求类型：POST
     * - 请求URL：http://localhost:8080/api/dialogs
     * - 请求体：JSON格式的对话数据，例如：
     *   {
     *     "user": {
     *       "uId": "user001"
     *     },
     *     "qaTimes": 0,
     *     "dAbstract": "这是一个新对话"
     *   }
     * - 响应：创建成功后返回创建的对话数据和201状态码
     *
     * @param dialog 对话实体，包含对话信息
     * @return 包含创建的对话数据和HTTP状态码的ResponseEntity对象
     */
    // @PostMapping：Spring注解，处理POST请求
    // 作用：当前端发送POST请求到/api/dialogs时，会调用这个方法
    @PostMapping

    // ResponseEntity：Spring用于封装HTTP响应的对象
    // 可以包含响应体、HTTP状态码、响应头等信息
    public ResponseEntity<Dialog> createDialog(
            // @Valid：JSR-303校验注解，用于验证请求体数据的合法性
            // 作用：如果dialog对象不符合验证规则（例如必填字段为空），会自动返回400错误
            @Valid
            
            // @RequestBody：Spring注解，用于将HTTP请求体转换为Java对象
            // 作用：自动将前端发送的JSON数据转换为Dialog对象
            @NotNull(message = "对话数据不能为空")
            @RequestBody Dialog dialog) {
        
        // 调用服务层的方法创建对话记录
        Dialog createdDialog = dialogService.createDialog(dialog);
        
        // 返回创建成功的对话数据和201状态码
        // HttpStatus.CREATED表示资源创建成功
        return new ResponseEntity<>(createdDialog, HttpStatus.CREATED);
    }

    /**
     * 根据对话ID查询单个对话
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/dialogs/{id}
     *   例如：http://localhost:8080/api/dialogs/1
     * - 响应：找到数据返回对话数据和200状态码；找不到返回404状态码
     *
     * @param id 对话ID，从URL路径中获取
     * @return 包含对话数据或错误状态码的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/dialogs/{id}时，会调用这个方法
    // {id}是路径参数，用于传递对话ID
    @GetMapping("/{id}")

    public ResponseEntity<Dialog> getDialogById(
            // @PathVariable：Spring注解，用于从URL路径中获取参数
            // 作用：将URL中的{id}值绑定到方法的id参数上
            @NotNull(message = "对话ID不能为空")
            @PathVariable Integer id) {
        
        // 调用服务层方法，根据对话ID查询对话记录
        // Optional是Java 8引入的类，用于处理可能为空的数据
        Optional<Dialog> optionalDialog = dialogService.getDialogById(id);
        
        // 使用Optional的map方法处理查询结果
        // 如果找到数据，返回200状态码和数据；否则返回404状态码
        return optionalDialog.map(
                // ResponseEntity::ok是方法引用，表示调用ResponseEntity.ok()方法
                // 作用：创建一个包含数据和200状态码的ResponseEntity对象
                ResponseEntity::ok
            )
            .orElseGet(
                // 如果optionalDialog为空，调用ResponseEntity.notFound().build()
                // 作用：创建一个包含404状态码的ResponseEntity对象
                () -> ResponseEntity.notFound().build());
    }

    /**
     * 根据用户ID查询该用户的所有对话
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/dialogs/user/{userId}
     *   例如：http://localhost:8080/api/dialogs/user/user001
     * - 响应：返回该用户的所有对话数据列表和200状态码
     *
     * @param userId 用户ID，从URL路径中获取
     * @return 包含对话列表数据的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/dialogs/user/{userId}时，会调用这个方法
    // {userId}是路径参数，用于传递用户ID
    @GetMapping("/user/{userId}")

    public ResponseEntity<List<Dialog>> getDialogsByUserId(
            // @PathVariable：从URL路径中获取用户ID
            @NotNull(message = "用户ID不能为空")
            @PathVariable String userId) {
        
        // 调用服务层方法，根据用户ID查询所有对话记录
        List<Dialog> dialogs = dialogService.getDialogsByUserId(userId);
        
        // 返回查询结果和200状态码
        return ResponseEntity.ok(dialogs);
    }

    /**
     * 查询所有对话（支持分页）
     * <p>
     * 前端调用方式：
     * - 请求类型：GET
     * - 请求URL：http://localhost:8080/api/dialogs?page=0&size=10&sort=dId,asc
     *   参数说明：
     *   - page：页码，从0开始
     *   - size：每页显示的记录数
     *   - sort：排序字段和排序方式，例如dId,asc表示按对话ID升序排序
     * - 响应：返回分页后的对话数据和200状态码
     *
     * @param pageable 分页参数，由Spring自动从请求参数中解析
     * @return 包含分页对话数据的ResponseEntity对象
     */
    // @GetMapping：Spring注解，处理GET请求
    // 作用：当前端发送GET请求到/api/dialogs时，会调用这个方法
    @GetMapping

    public ResponseEntity<Page<Dialog>> getAllDialogs(
            // Pageable：Spring Data JPA提供的分页参数类
            // 作用：自动从请求参数中解析出分页信息（page, size, sort）
            @NotNull(message = "分页参数不能为空")
            Pageable pageable) {
        
        // 调用服务层方法，获取所有对话记录（分页）
        Page<Dialog> dialogs = dialogService.getAllDialogs(pageable);
        
        // 返回分页结果和200状态码
        return ResponseEntity.ok(dialogs);
    }

    /**
     * 更新对话
     * <p>
     * 前端调用方式：
     * - 请求类型：PUT
     * - 请求URL：http://localhost:8080/api/dialogs/{id}
     *   例如：http://localhost:8080/api/dialogs/1
     * - 请求体：JSON格式的更新数据，例如：
     *   {
     *     "qaTimes": 5,
     *     "dAbstract": "更新后的对话摘要"
     *   }
     * - 响应：更新成功返回更新后的数据和200状态码；找不到数据返回404状态码
     *
     * @param id 对话ID，从URL路径中获取
     * @param dialog 更新的对话数据，由@RequestBody注解自动转换
     * @return 包含更新后数据的ResponseEntity对象
     */
    // @PutMapping：Spring注解，处理PUT请求
    // 作用：当前端发送PUT请求到指定URL时，会调用这个方法
    @PutMapping("/{id}")

    public ResponseEntity<Dialog> updateDialog(
            // @PathVariable：从URL路径中获取对话ID
            @NotNull(message = "对话ID不能为空")
            @PathVariable Integer id, 
            // @Valid：验证请求体数据的合法性
            // @RequestBody：将请求体JSON转换为Dialog对象
            @NotNull(message = "更新数据不能为空")
            @Valid @RequestBody Dialog dialog) {
        
        // 调用服务层方法，更新对话记录
        Dialog updatedDialog = dialogService.updateDialog(id, dialog);
        
        // 返回更新后的数据和200状态码
        return ResponseEntity.ok(updatedDialog);
    }

    /**
     * 删除对话
     * <p>
     * 前端调用方式：
     * - 请求类型：DELETE
     * - 请求URL：http://localhost:8080/api/dialogs/{id}
     *   例如：http://localhost:8080/api/dialogs/1
     * - 响应：删除成功返回204状态码（表示无内容）；找不到数据返回404状态码
     *
     * @param id 对话ID，从URL路径中获取
     * @return 包含状态码的ResponseEntity对象
     */
    // @DeleteMapping：Spring注解，处理DELETE请求
    // 作用：当前端发送DELETE请求到指定URL时，会调用这个方法
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteDialog(
            // @PathVariable：从URL路径中获取对话ID
            @NotNull(message = "对话ID不能为空")
            @PathVariable Integer id) {
        
        // 调用服务层方法，删除对话记录
        dialogService.deleteDialog(id);
        
        // 返回204状态码，表示删除成功且没有内容返回
        return ResponseEntity.noContent().build();
    }

}