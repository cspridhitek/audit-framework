package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLogEntity saveAuditLog(AuditLogEntity auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLogEntity> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public AuditLogEntity getAuditLogById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    public void deleteAuditLog(Long id) {
        auditLogRepository.deleteById(id);
    }

}