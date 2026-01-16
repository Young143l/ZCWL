package com.example.zcwl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应数据传输对象
 * 用于返回登录成功后的信息，包括username、userId和token
 */
public class LoginResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * JWT token
     */
    private String token;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 构造函数
     * @param token JWT token
     * @param userName 用户名
     * @param userId 用户ID
     */
    public LoginResponseDTO(String token, String userName, String userId) {
        this.token = token;
        this.userName = userName;
        this.userId = userId;
    }

    /**
     * 获取token
     * @return JWT token
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置token
     * @param token JWT token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置用户名
     * @param userName 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

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
}
