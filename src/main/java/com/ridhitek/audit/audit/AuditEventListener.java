package com.ridhitek.audit.audit;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.service.AuditService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {
    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        AuditLog auditLog = event.getAuditLog();
        auditService.saveAuditLog(auditLog);
    }
}