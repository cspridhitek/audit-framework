package com.ridhitek.audit.controller;

import com.ridhitek.audit.dto.AuditLogDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.service.AuditService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs() {
        List<AuditLog> auditLogs = auditService.getAllAuditLogs();
        List<AuditLogDTO> auditLogDTOS = auditLogs.stream().map(emp ->
                new AuditLogDTO(emp.getUserName(), emp.getAction(), emp.getOldValue(),
                        emp.getDeviceDetails(), emp.getTimestamp(), emp.getNewValue())).collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOS);
    }

    @GetMapping("/test")
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