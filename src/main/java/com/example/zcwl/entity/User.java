package com.example.zcwl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户实体类
 * 映射到数据库的users表，存储用户信息
 */
@Setter
@Getter
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "users")  // 指定映射的数据库表名
public class User implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 用户ID
     * 主键，长度20个字符，非空
     * -- GETTER --
     *  获取用户ID
     * -- SETTER --
     *  设置用户ID
     *
     */
    @Id  // 声明这是主键
    @Column(name = "u_id", length = 20, nullable = false)  // 映射到数据库的u_id列，指定长度和非空约束
    private String uId;

    /**
     * 用户名
     * 长度20个字符，非空，使用@NotBlank进行数据校验
     * -- GETTER --
     *  获取用户名
     * -- SETTER --
     *  设置用户名
     *
     */
    @NotBlank(message = "用户名不能为空")  // 数据校验，确保用户名不为空
    @Column(name = "name", length = 20, nullable = false)  // 映射到数据库的name列，指定长度和非空约束
    private String name;

    /**
     * 邮箱
     * 长度20个字符，非空，使用@NotBlank进行数据校验
     * -- GETTER --
     *  获取邮箱
     * -- SETTER --
     *  设置邮箱
     *
     */
    @NotBlank(message = "邮箱不能为空")  // 数据校验，确保邮箱不为空
    @Column(name = "email", length = 20, nullable = false)  // 映射到数据库的email列，指定长度和非空约束
    private String email;

    /**
     * 密码
     * 长度100个字符，非空，使用@NotBlank进行数据校验
     * 注意：使用BCryptPasswordEncoder加密后的密码长度会超过20个字符
     * -- GETTER --
     *  获取密码
     * -- SETTER --
     *  设置密码
     *
     */
    @NotBlank(message = "密码不能为空")  // 数据校验，确保密码不为空
    @Column(name = "password", nullable = false)  // 映射到数据库的password列，指定长度和非空约束
    private String password;

    /**
     * 头像
     * 使用TEXT类型，允许为空
     * -- GETTER --
     *  获取头像
     * -- SETTER --
     *  设置头像
     *
     */
    @Column(name = "avatar", columnDefinition = "TEXT")  // 映射到数据库的avatar列，使用TEXT类型，允许为空
    private String avatar;

    // getter和setter方法

}