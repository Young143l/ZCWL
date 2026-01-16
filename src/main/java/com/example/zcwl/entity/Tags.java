package com.example.zcwl.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 标签实体类
 * 映射到数据库的tags表，存储标签信息
 */
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "tags")  // 指定映射的数据库表名
public class Tags implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 标签ID
     * 主键，自增，非空
     */
    @Id  // 声明这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 指定主键生成策略为自增
    @Column(name = "t_id", nullable = false)  // 映射到数据库的t_id列，非空约束
    private Integer tId;

    /**
     * 标签名称
     * 长度20个字符，非空，唯一，使用@NotBlank进行数据校验
     */
    @NotBlank(message = "标签名称不能为空")  // 数据校验，确保标签名称不为空
    @Column(name = "name", length = 20, nullable = false, unique = true)  // 映射到数据库的name列，指定长度、非空约束和唯一约束
    private String name;

    // getter和setter方法
    /**
     * 获取标签ID
     * @return 标签ID
     */
    public Integer gettId() {
        return tId;
    }

    /**
     * 设置标签ID
     * @param tId 标签ID
     */
    public void settId(Integer tId) {
        this.tId = tId;
    }

    /**
     * 获取标签名称
     * @return 标签名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置标签名称
     * @param name 标签名称
     */
    public void setName(String name) {
        this.name = name;
    }

}