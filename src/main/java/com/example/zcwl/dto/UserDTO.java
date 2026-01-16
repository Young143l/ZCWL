package com.example.zcwl.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;

/**
 * 用户数据传输对象
 * 用于用户相关的请求和响应数据传输
 */
public class UserDTO implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     * 不能为空
     */
    @NotBlank(message = "用户名不能为空")  // 数据校验，确保用户名不为空
    private String username;

    /**
     * 密码
     * 不能为空
     */
    @NotBlank(message = "密码不能为空")  // 数据校验，确保密码不为空
    private String password;

    /**
     * 邮箱
     * 必须是有效的邮箱格式
     */
    @Email(message = "邮箱格式不正确")  // 数据校验，确保邮箱格式正确
    private String email;

    /**
     * 电话号码
     */
    private String phone;  // 可选字段，因为users数据库表中没有phone字段

    // getter和setter方法
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户ID
     * @param id 用户ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
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

    /**
     * 获取邮箱
     * @return 邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取电话号码
     * @return 电话号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置电话号码
     * @param phone 电话号码
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

}