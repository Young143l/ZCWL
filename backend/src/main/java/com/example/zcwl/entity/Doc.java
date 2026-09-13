package com.example.zcwl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档实体类
 * 映射到数据库的doc表，存储文档基本信息
 */
@Setter
@Getter
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "doc")  // 指定映射的数据库表名
public class Doc implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 文档ID
     * 主键，自增，非空
     * -- GETTER --
     *  获取文档ID
     * -- SETTER --
     *  设置文档ID
     *
     */
    @Id  // 声明这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 指定主键生成策略为自增
    @Column(name = "doc_id", nullable = false)  // 映射到数据库的doc_id列，非空约束
    private Integer docId;

    /**
     * 文档名称
     * 非空，唯一，使用@NotBlank进行数据校验
     * -- GETTER --
     *  获取文档名称
     * -- SETTER --
     *  设置文档名称
     *
     */
    @NotBlank(message = "文档名称不能为空")  // 数据校验，确保文档名称不为空
    @Column(name = "doc_name", columnDefinition = "TEXT", nullable = false, unique = true)  // 映射到数据库的doc_name列，使用TEXT类型，非空约束和唯一约束
    private String docName;

    /**
     * 文档摘要
     * 存储文档的简要描述
     * -- GETTER --
     *  获取文档摘要
     * -- SETTER --
     *  设置文档摘要
     *
     */
    @Column(name = "summary", columnDefinition = "TEXT")  // 映射到数据库的summary列，使用TEXT类型
    private String summary;

    /**
     * 文档图标
     * 存储文档的图标信息
     * -- GETTER --
     *  获取文档图标
     * -- SETTER --
     *  设置文档图标
     *
     */
    @Column(name = "icon", columnDefinition = "TEXT")  // 映射到数据库的icon列，使用TEXT类型
    private String icon;

}