package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog saveAuditLog(@Valid AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAllAuditLogs(@Min(0) int page, @Min(1) int size, @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_]+$") String sortBy, @Pattern(regexp = "^(asc|desc)$") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return auditLogRepository.findAll(pageable);
    }

    public Optional<AuditLog> getAuditLogById(@Min(1) Long id) {
        return auditLogRepository.findById(id);
    }

    public void deleteAuditLog(@Min(1) Long id) {
        auditLogRepository.deleteById(id);
    }
}
