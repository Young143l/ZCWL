package com.example.zcwl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 控制台应用代码生成项目实体类
 */
@Setter
@Getter
@Entity
@Table(name = "console_projects")
public class ConsoleProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cp_id", unique = true, nullable = false)
    @NotBlank(message = "cpId不能为空")
    @Size(max = 50, message = "cpId长度不能超过50个字符")
    private String cpId;

    @Column(name = "project_name", nullable = false)
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    private String projectName;

    @Column(name = "u_id", nullable = false)
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 50, message = "用户ID长度不能超过50个字符")
    private String userId;

    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    @Column(name = "type")
    @Size(max = 50, message = "类型长度不能超过50个字符")
    private String type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public ConsoleProject() {
    }

    public ConsoleProject(String cpId, String projectName, String userId, String code, String type) {
        this.cpId = cpId;
        this.projectName = projectName;
        this.userId = userId;
        this.code = code;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
