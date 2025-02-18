package com.example.audit.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // Action Taken

    @Column(nullable = false)
    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String signature;

    public AuditLog() {
        // this.timestamp = LocalDateTime.now();
    }

    // public AuditLog(String action, String oldValue, String newValue, String changedBy) {
    //     this.action = action;
    //     this.oldValue = oldValue;
    //     this.newValue = newValue;
    //     this.changedBy = changedBy;
    //     this.timestamp = LocalDateTime.now();
    // }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

}