package com.ridhitek.audit.event;

import com.ridhitek.audit.entity.AuditLogEntity;
import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {
    private final AuditLogEntity auditLog;

    public AuditEvent(Object source, AuditLogEntity auditLog) {
        super(source);
        this.auditLog = auditLog;
    }

    public AuditLogEntity getAuditLog() {
        return auditLog;
    }
}