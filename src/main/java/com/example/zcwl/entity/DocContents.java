package com.example.zcwl.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 文档内容实体类
 * 映射到数据库的doc_contents表，存储文档的章节内容
 */
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "doc_contents")  // 指定映射的数据库表名
public class DocContents implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 复合主键
     * 使用嵌入式主键类DocContentsId
     */
    @EmbeddedId  // 声明使用嵌入式主键
    private DocContentsId id;

    /**
     * 章节名称
     * 长度20个字符，非空，使用@NotBlank进行数据校验
     */
    @NotBlank(message = "章节名称不能为空")  // 数据校验，确保章节名称不为空
    @Column(name = "name", length = 20, nullable = false)  // 映射到数据库的name列，指定长度和非空约束
    private String name;

    /**
     * 章节内容
     * 非空，使用@NotBlank进行数据校验
     */
    @NotBlank(message = "章节内容不能为空")  // 数据校验，确保章节内容不为空
    @Column(name = "content", nullable = false)  // 映射到数据库的content列，非空约束
    private String content;

    /**
     * 关联的文档
     * 多对一关系，使用懒加载，关联到Doc实体
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 声明多对一关系，使用懒加载
    @MapsId("docId")  // 映射嵌入式主键中的docId字段
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", nullable = false, foreignKey = @ForeignKey(name = "doc_contents_doc_id_fkey", foreignKeyDefinition = "FOREIGN KEY (doc_id) REFERENCES doc(doc_id) ON DELETE CASCADE"))  // 指定外键关联，添加ON DELETE CASCADE
    private Doc doc;

    /**
     * 获取复合主键
     * @return 复合主键对象
     */
    public DocContentsId getId() {
        return id;
    }

    /**
     * 设置复合主键
     * @param id 复合主键对象
     */
    public void setId(DocContentsId id) {
        this.id = id;
    }

    /**
     * 获取章节名称
     * @return 章节名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置章节名称
     * @param name 章节名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取章节内容
     * @return 章节内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置章节内容
     * @param content 章节内容
     */
    public void setContent(String content) {
        this.content = content;
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
     * 嵌入式主键类
     * 用于DocContents实体的复合主键
     */
    @Embeddable  // 声明这是一个嵌入式主键类
    public static class DocContentsId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

        /**
         * 文档ID
         * 作为复合主键的一部分，关联到doc表的doc_id字段
         */
        @Column(name = "doc_id", nullable = false)  // 映射到数据库的doc_id列，非空约束
        private Integer docId;

        /**
         * 章节ID
         * 作为复合主键的一部分，存储章节编号
         */
        @Column(name = "chapter_id", nullable = false)  // 映射到数据库的chapter_id列，非空约束
        private Integer chapterId;

        /**
         * 默认构造器
         * JPA要求必须提供默认构造器
         */
        public DocContentsId() {
        }

        /**
         * 带参数的构造器
         * 用于创建DocContentsId对象
         * @param docId 文档ID
         * @param chapterId 章节ID
         */
        public DocContentsId(Integer docId, Integer chapterId) {
            this.docId = docId;
            this.chapterId = chapterId;
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
         * 获取章节ID
         * @return 章节ID
         */
        public Integer getChapterId() {
            return chapterId;
        }

        /**
         * 设置章节ID
         * @param chapterId 章节ID
         */
        public void setChapterId(Integer chapterId) {
            this.chapterId = chapterId;
        }

        /**
         * 重写equals方法
         * 用于判断两个DocContentsId对象是否相等
         * @param o 比较的对象
         * @return 是否相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DocContentsId that = (DocContentsId) o;

            if (!Objects.equals(docId, that.docId)) return false;
            return Objects.equals(chapterId, that.chapterId);
        }

        /**
         * 重写hashCode方法
         * 用于生成DocContentsId对象的哈希码
         * @return 哈希码
         */
        @Override
        public int hashCode() {
            int result = docId != null ? docId.hashCode() : 0;
            result = 31 * result + (chapterId != null ? chapterId.hashCode() : 0);
            return result;
        }
    }

}