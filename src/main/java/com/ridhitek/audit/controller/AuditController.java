package com.ridhitek.audit.controller;

import com.ridhitek.audit.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.service.AuditService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@RestController
@CrossOrigin(origins = "*")

@RequestMapping("/api/audit")
@Validated
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> getAllAuditLogs(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam(name = "sortBy", defaultValue = "timestamp") @Pattern(regexp = "^[a-zA-Z0-9_]+$") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") @Pattern(regexp = "^(asc|desc)$") String sortDirection) {

        Page<AuditLog> auditLogs = auditService.getAllAuditLogs(page, size, sortBy, sortDirection);

        Page<AuditLogDTO> auditLogDTOPage = auditLogs.map(auditLog ->
                new AuditLogDTO(
                        auditLog.getUserName(),
                        auditLog.getAction(),
                        auditLog.getOldValue(),
                        auditLog.getDeviceDetails(),
                        auditLog.getTimestamp(),
                        auditLog.getNewValue()
                ));
        return ResponseEntity.ok(auditLogDTOPage);
    }
}