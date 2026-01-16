package com.example.zcwl.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 文档标签关联实体类
 * 映射到数据库的doc_tags表，存储文档与标签的多对多关联关系
 */
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "doc_tags")  // 指定映射的数据库表名
public class DocTags implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 复合主键
     * 使用嵌入式主键类DocTagsId
     */
    @EmbeddedId  // 声明使用嵌入式主键
    private DocTagsId id;

    /**
     * 关联的文档
     * 多对一关系，使用懒加载，关联到Doc实体
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 声明多对一关系，使用懒加载
    @MapsId("docId")  // 映射嵌入式主键中的docId字段
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", nullable = false)  // 指定外键关联
    private Doc doc;

    /**
     * 关联的标签
     * 多对一关系，使用懒加载，关联到Tags实体
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 声明多对一关系，使用懒加载
    @MapsId("tId")  // 映射嵌入式主键中的tId字段
    @JoinColumn(name = "t_id", referencedColumnName = "t_id", nullable = false)  // 指定外键关联
    private Tags tags;

    /**
     * 获取复合主键
     * @return 复合主键对象
     */
    public DocTagsId getId() {
        return id;
    }

    /**
     * 设置复合主键
     * @param id 复合主键对象
     */
    public void setId(DocTagsId id) {
        this.id = id;
    }

    /**
     * 获取关联的文档
     * @return 文档对象
     */
    public Doc getDoc() {
        return doc;
    }

    /**
     * 设置关联的文档
     * @param doc 文档对象
     */
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    /**
     * 获取关联的标签
     * @return 标签对象
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * 设置关联的标签
     * @param tags 标签对象
     */
    public void setTags(Tags tags) {
        this.tags = tags;
    }

    /**
     * 嵌入式主键类
     * 用于DocTags实体的复合主键
     */
    @Embeddable  // 声明这是一个嵌入式主键类
    public static class DocTagsId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

        /**
         * 文档ID
         * 作为复合主键的一部分，关联到doc表的doc_id字段
         */
        @Column(name = "doc_id", nullable = false)  // 映射到数据库的doc_id列，非空约束
        private Integer docId;

        /**
         * 标签ID
         * 作为复合主键的一部分，关联到tags表的t_id字段
         */
        @Column(name = "t_id", nullable = false)  // 映射到数据库的t_id列，非空约束
        private Integer tId;

        /**
         * 默认构造器
         * JPA要求必须提供默认构造器
         */
        public DocTagsId() {
        }

        /**
         * 带参数的构造器
         * 用于创建DocTagsId对象
         * @param docId 文档ID
         * @param tId 标签ID
         */
        public DocTagsId(Integer docId, Integer tId) {
            this.docId = docId;
            this.tId = tId;
        }

        /**
         * 获取文档ID
         * @return 文档ID
         */
        public Integer getDocId() {
            return docId;
        }

        /**
         * 设置文档ID
         * @param docId 文档ID
         */
        public void setDocId(Integer docId) {
            this.docId = docId;
        }

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
         * 重写equals方法
         * 用于判断两个DocTagsId对象是否相等
         * @param o 比较的对象
         * @return 是否相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DocTagsId that = (DocTagsId) o;

            if (!Objects.equals(docId, that.docId)) return false;
            return Objects.equals(tId, that.tId);
        }

        /**
         * 重写hashCode方法
         * 用于生成DocTagsId对象的哈希码
         * @return 哈希码
         */
        @Override
        public int hashCode() {
            int result = docId != null ? docId.hashCode() : 0;
            result = 31 * result + (tId != null ? tId.hashCode() : 0);
            return result;
        }
    }

}