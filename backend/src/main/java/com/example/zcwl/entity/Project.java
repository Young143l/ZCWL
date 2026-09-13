package com.example.zcwl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 项目实体类
 */
@Setter
@Getter
@Entity
@Table(name = "projects")
public class Project {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "description")
    private String description;

    @Column(name = "url")
    private String url;

    @Column(name = "u_id", nullable = false)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status")
    private String status;

    @Column(name = "cloud_storage_id")
    private String cloudStorageId;

    // 显式添加cloudStorageId的setter方法
    public void setCloudStorageId(String cloudStorageId) {
        this.cloudStorageId = cloudStorageId;
    }

    // 显式添加cloudStorageId的getter方法
    public String getCloudStorageId() {
        return cloudStorageId;
    }

    // 构造函数
    public Project() {
    }

    public Project(String projectName, String description, String url, String userId) {
        this.projectName = projectName;
        this.url = url;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "active";
    }

}