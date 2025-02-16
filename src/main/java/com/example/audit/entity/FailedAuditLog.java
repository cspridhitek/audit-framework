package com.example.audit.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getActualTime() {
        return actualTime;
    }

    public void setActualTime(LocalDateTime actualTime) {
        this.actualTime = actualTime;
    }
}