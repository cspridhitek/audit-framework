package com.ridhitek.audit.listener;

import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.event.AuditEvent;
import com.ridhitek.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {
    private final AuditService auditService;

    @Autowired
    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        AuditLogEntity auditLog = event.getAuditLog();
        auditService.saveAuditLog(auditLog);
    }
}