package com.example.zcwl.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_id")
    private Long id;

    @Column(name = "u_id", nullable = false)
    private String uId;

    @Column(name = "d_id", nullable = false)
    private Long dId;

    @Column(name = "c_id", nullable = false)
    private Long cId;

    @Column(name = "from_u_name", nullable = false)
    private String fromUName;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public Long getDId() {
        return dId;
    }

    public void setDId(Long dId) {
        this.dId = dId;
    }

    public Long getCId() {
        return cId;
    }

    public void setCId(Long cId) {
        this.cId = cId;
    }

    public String getFromUName() {
        return fromUName;
    }

    public void setFromUName(String fromUName) {
        this.fromUName = fromUName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}