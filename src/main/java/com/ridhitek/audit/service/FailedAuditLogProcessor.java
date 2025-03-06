package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FailedAuditLogProcessor {

    private final AuditLogProducer auditLogProducer;
    private final FailedAuditLogRepository failedAuditLogRepository;

    public FailedAuditLogProcessor(AuditLogProducer auditLogProducer, FailedAuditLogRepository failedAuditLogRepository) {
        this.auditLogProducer = auditLogProducer;
        this.failedAuditLogRepository = failedAuditLogRepository;
    }

    @Scheduled(fixedDelay = 60000)  // Run every 1 minute
    public void retryFailedLogs() {
        List<FailedAuditLog> failedLogs = failedAuditLogRepository.findAll();

        for (FailedAuditLog log : failedLogs) {
            try {
                AuditLog auditLog = new AuditLog();
                auditLog.setAction(log.getAction());
                auditLog.setUserName(log.getUserName());
                auditLog.setDeviceDetails(log.getDeviceDetails());
                auditLog.setTimestamp(log.getTimestamp());
                auditLog.setNewValue(log.getNewValue());
                auditLog.setOldValue(log.getOldValue());
                auditLog.setSignature(log.getSignature());
                auditLogProducer.logToKafka(auditLog);
                failedAuditLogRepository.delete(log); // Remove log after successful reprocessing
                System.out.println("Retried and sent failed audit log successfully.");
            } catch (Exception e) {
                System.err.println("Retrying failed audit log failed again: " + log.getFailureReason());
            }
        }
    }
}
