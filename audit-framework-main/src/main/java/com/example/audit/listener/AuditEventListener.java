package com.example.audit.listener;

import com.example.audit.entity.AuditLog;
import com.example.audit.event.AuditEvent;
import com.example.audit.service.AuditService;
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
        AuditLog auditLog = event.getAuditLog();
        auditService.saveAuditLog(auditLog);
    }
}