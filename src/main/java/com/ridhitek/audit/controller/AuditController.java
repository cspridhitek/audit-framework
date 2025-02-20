package com.ridhitek.audit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ridhitek.audit.annotation.Auditable;
import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.service.AuditService;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogEntity>> getAllAuditLogs() {
        List<AuditLogEntity> auditLogs = auditService.getAllAuditLogs();
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/test")
    @Auditable(action = "TEST_ACTION")
    public void testAudit() {
        
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogEntity> getAuditLogById(@PathVariable Long id) {
        AuditLogEntity auditLog = auditService.getAuditLogById(id);
        return auditLog != null ? ResponseEntity.ok(auditLog) : ResponseEntity.notFound().build();
    }

    @PostMapping()
    public ResponseEntity<AuditLogEntity> createAuditLog(@RequestBody AuditLogEntity auditLog) {
        AuditLogEntity createdAuditLog = auditService.saveAuditLog(auditLog);
        return ResponseEntity.status(201).body(createdAuditLog);
    }
}