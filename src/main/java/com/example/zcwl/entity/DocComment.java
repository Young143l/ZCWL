package com.example.zcwl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文档评论实体类
 */
@Setter
@Getter
@Entity
@Table(name = "doc_comment")
public class DocComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "d_id", nullable = false)
    private Long dId;

    @Column(name = "c_id", nullable = false)
    private Long cId;

    @Column(name = "u_id", nullable = false)
    private String uId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "fa", nullable = false)
    private Long fa;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public DocComment() {
    }

    public DocComment(Long dId, Long cId, String uId, String email, Long fa, String comment) {
        this.dId = dId;
        this.cId = cId;
        this.uId = uId;
        this.email = email;
        this.fa = fa;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 自动更新时间
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
