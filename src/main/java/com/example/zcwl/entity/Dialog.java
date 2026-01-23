package com.example.zcwl.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * 对话实体类
 * 映射到数据库的dialog表，存储用户对话信息
 */
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "dialog")  // 指定映射的数据库表名
public class Dialog implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 对话ID
     * 主键，自增，非空
     */
    @Id  // 声明这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 指定主键生成策略为自增
    @Column(name = "d_id", nullable = false)  // 映射到数据库的d_id列，非空约束
    private Integer dId;

    /**
     * 关联的用户
     * 多对一关系，使用懒加载，关联到User实体
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 声明多对一关系，使用懒加载
    @JoinColumn(name = "u_id", referencedColumnName = "u_id", nullable = false, foreignKey = @ForeignKey(name = "dialog_u_id_fkey", foreignKeyDefinition = "FOREIGN KEY (u_id) REFERENCES users(u_id) ON DELETE CASCADE"))  // 指定外键关联，添加ON DELETE CASCADE
    private User user;

    /**
     * 问答次数
     * 记录对话中的问答次数
     */
    @Column(name = "qa_times")  // 映射到数据库的qa_times列
    private Integer qaTimes;

    /**
     * 对话摘要
     * 存储对话的简要描述
     */
    @Column(name = "d_abstract")  // 映射到数据库的d_abstract列
    private String dAbstract;

    /**
     * 获取对话ID
     * @return 对话ID
     */
    public Integer getdId() {
        return dId;
    }

    /**
     * 设置对话ID
     * @param dId 对话ID
     */
    public void setdId(Integer dId) {
        this.dId = dId;
    }

    /**
     * 获取关联的用户
     * @return 用户对象
     */
    public User getUser() {
        return user;
    }

    /**
     * 设置关联的用户
     * @param user 用户对象
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 获取问答次数
     * @return 问答次数
     */
    public Integer getQaTimes() {
        return qaTimes;
    }

    /**
     * 设置问答次数
     * @param qaTimes 问答次数
     */
    public void setQaTimes(Integer qaTimes) {
        this.qaTimes = qaTimes;
    }

    /**
     * 获取对话摘要
     * @return 对话摘要
     */
    public String getdAbstract() {
        return dAbstract;
    }

    /**
     * 设置对话摘要
     * @param dAbstract 对话摘要
     */
    public void setdAbstract(String dAbstract) {
        this.dAbstract = dAbstract;
    }

}