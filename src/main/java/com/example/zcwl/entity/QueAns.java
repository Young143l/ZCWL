package com.example.zcwl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 问答实体类
 * 映射到数据库的que_ans表，存储问答记录
 */
@Setter
@Getter
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "que_ans")  // 指定映射的数据库表名
public class QueAns implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 复合主键
     * 使用嵌入式主键类QueAnsId，包含对话ID和问答次数
     * -- GETTER --
     *  获取复合主键
     * -- SETTER --
     *  设置复合主键
     *
     */
    @EmbeddedId  // 声明使用嵌入式主键
    private QueAnsId id;

    /**
     * 问答时间
     * 数据库默认值为当前时间
     * -- GETTER --
     *  获取问答时间
     * -- SETTER --
     *  设置问答时间
     *
     */
    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    // @Column：指定数据库列名、非空约束和列定义
    private LocalDateTime date;

    /**
     * 问题内容
     * 不能为空，使用@NotBlank进行数据校验
     * -- GETTER --
     *  获取问题内容
     * -- SETTER --
     *  设置问题内容
     *
     */
    @NotBlank(message = "问题不能为空")  // 数据校验，确保问题内容不为空
    @Column(name = "que", nullable = false, columnDefinition = "TEXT")  // 映射到数据库的que列，使用TEXT类型支持长文本，非空约束
    private String que;

    /**
     * 回答内容
     * 不能为空，使用@NotBlank进行数据校验
     * -- GETTER --
     *  获取回答内容
     * -- SETTER --
     *  设置回答内容
     *
     */
    @NotBlank(message = "回答不能为空")  // 数据校验，确保回答内容不为空
    @Column(name = "ans", nullable = false, columnDefinition = "TEXT")  // 映射到数据库的ans列，使用TEXT类型支持长文本，非空约束
    private String ans;

    /**
     * 关联的对话
     * 多对一关系，多个问答属于一个对话
     * -- GETTER --
     *  获取关联的对话
     * -- SETTER --
     *  设置关联的对话
     *
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 多对一关联，使用懒加载（LAZY）提高性能
    @MapsId("dId")  // 映射主键中的dId字段到Dialog实体
    @JoinColumn(name = "d_id", referencedColumnName = "d_id", nullable = false, foreignKey = @ForeignKey(name = "que_ans_d_id_fkey", foreignKeyDefinition = "FOREIGN KEY (d_id) REFERENCES dialog(d_id) ON DELETE CASCADE"))
    // @JoinColumn：指定外键列名和参照的主键列名，添加ON DELETE CASCADE
    private Dialog dialog;

    // getter和setter方法

    /**
     * 嵌入式主键类
     * 用于表示que_ans表的复合主键（d_id, times）
     */
    @Embeddable  // 声明这是一个嵌入式主键类
    public static class QueAnsId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;  // 序列化版本号

        /**
         * 对话ID
         * 对应que_ans表的d_id列
         */
        @Column(name = "d_id", nullable = false)  // 映射到数据库的d_id列，非空约束
        private Integer dId;

        /**
         * 问答次数
         * 对应que_ans表的times列
         * -- GETTER --
         *  获取问答次数
         * -- SETTER --
         *  设置问答次数
         *
         */
        @Setter
        @Getter
        @Column(name = "times", nullable = false)  // 映射到数据库的times列，非空约束
        private Integer times;

        /**
         * 默认构造器
         * 必须提供，用于JPA实例化对象
         */
        public QueAnsId() {
        }

        /**
         * 带参数的构造器
         * 用于创建主键对象
         * @param dId 对话ID
         * @param times 问答次数
         */
        public QueAnsId(Integer dId, Integer times) {
            this.dId = dId;
            this.times = times;
        }

        // getter和setter方法
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
         * 重写equals方法
         * 用于比较两个主键对象是否相等
         * @param o 比较对象
         * @return 是否相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueAnsId queAnsId = (QueAnsId) o;

            if (!Objects.equals(dId, queAnsId.dId)) return false;
            return Objects.equals(times, queAnsId.times);
        }

        /**
         * 重写hashCode方法
         * 用于生成主键对象的哈希值
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            int result = dId != null ? dId.hashCode() : 0;
            result = 31 * result + (times != null ? times.hashCode() : 0);
            return result;
        }
    }

}