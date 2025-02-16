package com.example.audit.event;

import com.example.audit.entity.AuditLog;
import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {
    private final AuditLog auditLog;

    public AuditEvent(Object source, AuditLog auditLog) {
        super(source);
        this.auditLog = auditLog;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }
}