package com.example.zcwl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 简单前端代码生成项目实体类
 */
@Setter
@Getter
@Entity
@Table(name = "simple_frontend_projects")
public class SimpleFrontendProject {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sf_id", unique = true, nullable = false)
    private String sfId;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "u_id", nullable = false)
    private String userId;

    @Column(name = "html", columnDefinition = "TEXT")
    private String html;

    @Column(name = "css", columnDefinition = "TEXT")
    private String css;

    @Column(name = "javascript", columnDefinition = "TEXT")
    private String javascript;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public SimpleFrontendProject() {
    }

    public SimpleFrontendProject(String sfId, String projectName, String userId, String html, String css, String javascript) {
        this.sfId = sfId;
        this.projectName = projectName;
        this.userId = userId;
        this.html = html;
        this.css = css;
        this.javascript = javascript;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}