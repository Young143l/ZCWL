package com.example.zcwl.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应数据传输对象
 * 用于返回登录成功后的信息，包括username、userId和token
 */
@Setter
@Getter
public class LoginResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * JWT token
     * -- GETTER --
     *  获取token
     * -- SETTER --
     *  设置token
     *
     */
    private String token;

    /**
     * 用户名
     * -- GETTER --
     *  获取用户名
     * -- SETTER --
     *  设置用户名
     *
     */
    private String userName;

    /**
     * 用户 ID
     * -- GETTER --
     *  获取用户 ID
     * -- SETTER --
     *  设置用户 ID
     *
     */
    private String userId;
    
    /**
     * 邮箱
     * -- GETTER --
     *  获取邮箱
     * -- SETTER --
     *  设置邮箱
     *
     */
    private String email;

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
     * 构造函数
     * @param token JWT token
     * @param userName 用户名
     * @param userId 用户 ID
     * @param email 邮箱
     */
    public LoginResponseDTO(String token, String userName, String userId, String email) {
        this.token = token;
        this.userName = userName;
        this.userId = userId;
        this.email = email;
    }

}
