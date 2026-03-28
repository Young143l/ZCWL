package com.example.zcwl.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户数据传输对象
 * 用于用户相关的请求和响应数据传输
 */
@Setter
@Getter
public class UserDTO implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号

    /**
     * 用户ID
     * -- GETTER --
     *  获取用户ID
     * -- SETTER --
     *  设置用户ID
     *
     */
    private Long id;

    /**
     * 用户名
     * 不能为空
     * -- GETTER --
     *  获取用户名
     * -- SETTER --
     *  设置用户名
     *
     */
    @NotBlank(message = "用户名不能为空")  // 数据校验，确保用户名不为空
    @Size(min = 6, max = 20, message = "用户名长度必须在6-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文字符")
    private String userName;

    /**
     * 密码
     * 不能为空
     * -- GETTER --
     *  获取密码
     * -- SETTER --
     *  设置密码
     *
     */
    @NotBlank(message = "密码不能为空")  // 数据校验，确保密码不为空
    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    private String password;

    /**
     * 邮箱
     * 必须是有效的邮箱格式
     * -- GETTER --
     *  获取邮箱
     * -- SETTER --
     *  设置邮箱
     *
     */
    @Email(message = "邮箱格式不正确")  // 数据校验，确保邮箱格式正确
    @NotBlank(message = "邮箱不能为空")
    @Size(max = 20, message = "邮箱长度不能超过20个字符")
    private String email;
    

    // getter和setter方法

}