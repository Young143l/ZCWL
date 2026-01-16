package com.example.zcwl.dto;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求数据传输对象
 * 用于接收前端发送的登录信息
 */
public class LoginRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 不能为空
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 密码
     * 不能为空
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取密码
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
