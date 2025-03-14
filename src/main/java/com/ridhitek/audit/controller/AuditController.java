package com.ridhitek.audit.controller;

import com.ridhitek.audit.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<AuditLogDTO>> getAllAuditLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "timestamp") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection) {

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