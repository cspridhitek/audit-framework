package com.example.audit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.audit.annotation.Auditable;
import com.example.audit.entity.AuditLog;
import com.example.audit.service.AuditService;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> auditLogs = auditService.getAllAuditLogs();
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/test")
    @Auditable(action = "TEST_ACTION")
    public void testAudit() {
        
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        AuditLog auditLog = auditService.getAuditLogById(id);
        return auditLog != null ? ResponseEntity.ok(auditLog) : ResponseEntity.notFound().build();
    }

    @PostMapping()
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        AuditLog createdAuditLog = auditService.saveAuditLog(auditLog);
        return ResponseEntity.status(201).body(createdAuditLog);
    }
}