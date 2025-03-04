package com.ridhitek.audit.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
public class FailedAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actor;
    private String action;
    private String details;
    private String errorMessage;
    private LocalDateTime actualTime;

    // Constructors, getters, and setters
    public FailedAuditLog() {}

    public FailedAuditLog(String actor, String action, String details, String errorMessage, LocalDateTime actualTime) {
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.errorMessage = errorMessage;
        this.actualTime = actualTime;
    }

}