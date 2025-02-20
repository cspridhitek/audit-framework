package com.ridhitek.audit.config;

import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.service.AuditService;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAppender {

    private final AuditService auditService;

    public DatabaseAppender(AuditService auditService) {
        this.auditService = auditService;
    }

    public void logToDatabase(String actor, String action, String details) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setAction(action);
        auditLog.setUserName(actor);

        auditService.saveAuditLog(auditLog);
    }
}