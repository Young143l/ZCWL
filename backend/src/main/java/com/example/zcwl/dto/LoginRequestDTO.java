package com.example.zcwl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求数据传输对象
 * 用于接收前端发送的登录信息
 */
@Setter
@Getter
public class LoginRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 不能为空
     * -- GETTER --
     *  获取用户ID
     * -- SETTER --
     *  设置用户ID
     *
     */
    @NotBlank(message = "用户ID不能为空")
    @Size(min = 6, max = 20, message = "用户ID长度必须在6-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$", message = "用户ID只能包含字母、数字、下划线和中文字符")
    private String userId;

    /**
     * 密码
     * 不能为空
     * -- GETTER --
     *  获取密码
     * -- SETTER --
     *  设置密码
     *
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    private String password;

}
