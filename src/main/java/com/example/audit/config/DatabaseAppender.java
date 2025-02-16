package com.example.audit.config;

import com.example.audit.entity.AuditLog;
import com.example.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAppender {

    private final AuditService auditService;

    @Autowired
    public DatabaseAppender(AuditService auditService) {
        this.auditService = auditService;
    }

    public void logToDatabase(String actor, String action, String details) {
        AuditLog auditLog = new AuditLog(action, "", details, actor);
        auditService.saveAuditLog(auditLog);
    }
}