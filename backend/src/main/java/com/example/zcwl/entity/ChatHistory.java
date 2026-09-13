package com.example.zcwl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天历史记录实体类
 * 映射到数据库的chat_history表，存储用户每轮对话的所有问答的提取结果
 */
@Setter
@Getter
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "chat_history")  // 指定映射的数据库表名
public class ChatHistory implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 对话ID
     * 主键，与Dialog的d_id关联
     */
    @Id  // 声明这是主键
    @Column(name = "d_id", nullable = false)  // 映射到数据库的d_id列，非空约束
    private Integer dId;

    /**
     * 关联的对话
     * 一对一关系，使用懒加载，关联到Dialog实体
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // 使用dId作为主键，与Dialog的d_id关联
    @JoinColumn(name = "d_id", referencedColumnName = "d_id", foreignKey = @ForeignKey(name = "chat_history_d_id_fkey", foreignKeyDefinition = "FOREIGN KEY (d_id) REFERENCES dialog(d_id) ON DELETE CASCADE"))  // 指定外键关联，添加ON DELETE CASCADE
    private Dialog dialog;

    /**
     * 提取的提示词
     * 存储用户该轮对话的所有问答的提取结果
     * -- GETTER --
     *  获取提取的提示词
     * -- SETTER --
     *  设置提取的提示词
     *
     */
    @Column(name = "extracted_prompt", columnDefinition = "TEXT")  // 映射到数据库的extracted_prompt列，使用TEXT类型支持长文本
    private String extractedPrompt;

    /**
     * 创建时间
     * 记录提取的时间
     * -- GETTER --
     *  获取创建时间
     * -- SETTER --
     *  设置创建时间
     *
     */
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}
