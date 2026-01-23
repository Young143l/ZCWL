package com.example.zcwl.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 用户实体类
 * 映射到数据库的users表，存储用户信息
 */
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "users")  // 指定映射的数据库表名
public class User implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 用户ID
     * 主键，长度50个字符，非空
     */
    @Id  // 声明这是主键
    @Column(name = "u_id", length = 50, nullable = false)  // 映射到数据库的u_id列，指定长度和非空约束
    private String uId;

    /**
     * 用户名
     * 长度50个字符，非空，使用@NotBlank进行数据校验
     */
    @NotBlank(message = "用户名不能为空")  // 数据校验，确保用户名不为空
    @Column(name = "name", length = 50, nullable = false)  // 映射到数据库的name列，指定长度和非空约束
    private String name;

    /**
     * 邮箱
     * 长度50个字符，非空，使用@NotBlank进行数据校验
     */
    @NotBlank(message = "邮箱不能为空")  // 数据校验，确保邮箱不为空
    @Column(name = "email", length = 50, nullable = false)  // 映射到数据库的email列，指定长度和非空约束
    private String email;

    /**
     * 密码
     * 长度100个字符，非空，使用@NotBlank进行数据校验
     * 注意：使用BCryptPasswordEncoder加密后的密码长度会超过20个字符
     */
    @NotBlank(message = "密码不能为空")  // 数据校验，确保密码不为空
    @Column(name = "password", length = 255, nullable = false)  // 映射到数据库的password列，指定长度和非空约束
    private String password;

    // getter和setter方法
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public String getUId() {
        return uId;
    }

    /**
     * 设置用户ID
     * @param uId 用户ID
     */
    public void setUId(String uId) {
        this.uId = uId;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置用户名
     * @param name 用户名
     */
    public void setName(String name) {
        this.name = name;
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