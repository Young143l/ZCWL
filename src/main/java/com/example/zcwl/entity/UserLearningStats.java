package com.example.zcwl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "user_learning_stats")
public class UserLearningStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "u_id", length = 20)
    private String userId;

    @Column(name = "total_docs")
    private Integer totalDocs;

    @Column(name = "completed_chapters")
    private Integer completedChapters;

    @Column(name = "streak_days")
    private Integer streakDays;

    @Column(name = "last_study_date")
    private LocalDate lastStudyDate;

    @Column(name = "total_study_days")
    private Integer totalStudyDays;

    public UserLearningStats() {
        this.totalDocs = 0;
        this.completedChapters = 0;
        this.streakDays = 0;
        this.totalStudyDays = 0;
    }
}
