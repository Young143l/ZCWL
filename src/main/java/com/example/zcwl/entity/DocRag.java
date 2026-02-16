package com.example.zcwl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * DocRag实体类
 * 映射到doc_rag表，存储文档向量数据
 */
@Setter
@Getter
@Entity
@Table(name = "doc_rag")
public class DocRag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     * -- GETTER --
     *  获取文档ID
     * -- SETTER --
     *  设置文档ID
     *
     */
    @Id
    @Column(name = "id")
    private Integer id;

    /**
     * 文档片段内容
     * -- GETTER --
     *  获取文档片段内容
     * -- SETTER --
     *  设置文档片段内容
     *
     */
    @Column(name = "chunk", columnDefinition = "text")
    private String chunk;

    /**
     * 文档向量
     * 注意：PostgreSQL的vector类型在JPA中没有直接对应类型，使用String存储
     * -- GETTER --
     *  获取文档向量
     * -- SETTER --
     *  设置文档向量
     *
     */
    @Column(name = "vector", columnDefinition = "vector")
    private String vector;

    @Override
    public String toString() {
        return "DocRag{" +
                "id=" + id +
                ", chunk='" + (chunk != null ? chunk.substring(0, Math.min(50, chunk.length())) + "..." : "null") + '\'' +
                ", vector='" + (vector != null ? vector.substring(0, Math.min(50, vector.length())) + "..." : "null") + '\'' +
                '}';
    }
}
