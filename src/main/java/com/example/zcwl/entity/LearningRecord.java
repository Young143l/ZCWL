package com.example.zcwl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "learning_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"u_id", "doc_id", "chapter_id"})
})
public class LearningRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "u_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Column(name = "chapter_id")
    private Integer chapterId;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "last_position")
    private Integer lastPosition;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LearningRecord() {
        this.progress = 0;
        this.lastPosition = 0;
        this.status = "learning";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
