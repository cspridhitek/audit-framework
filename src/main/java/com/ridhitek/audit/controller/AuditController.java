package com.ridhitek.audit.controller;

import com.ridhitek.audit.dto.AuditLogDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ridhitek.audit.annotation.Auditable;
import com.ridhitek.audit.entity.AuditLogEntity;
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
        List<AuditLogEntity> auditLogs = auditService.getAllAuditLogs();
        List<AuditLogDTO> auditLogDTOS = auditLogs.stream().map(emp ->
                new AuditLogDTO(emp.getUserName(), emp.getAction(), emp.getOldValue(),
                        emp.getDeviceDetails(), emp.getTimestamp(), emp.getNewValue())).collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOS);
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