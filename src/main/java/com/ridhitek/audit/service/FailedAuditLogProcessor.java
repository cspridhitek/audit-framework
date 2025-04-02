package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FailedAuditLogProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FailedAuditLogProcessor.class);
    private static final int PAGE_SIZE = 10; // Process 10 records at a time

    private final AuditLogProducer auditLogProducer;
    private final FailedAuditLogRepository failedAuditLogRepository;

    public FailedAuditLogProcessor(AuditLogProducer auditLogProducer, FailedAuditLogRepository failedAuditLogRepository) {
        this.auditLogProducer = auditLogProducer;
        this.failedAuditLogRepository = failedAuditLogRepository;
    }

    @Scheduled(fixedDelay = 60000)  // Runs every 1 minute
    public void retryFailedLogs() {
        long totalRecords = failedAuditLogRepository.count();
        if (totalRecords == 0) {
            logger.info("No failed audit logs to retry.");
            return;
        }

        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        logger.info("Processing {} failed audit logs in {} batches.", totalRecords, totalPages);

        int currentPage = 0;

        while (true) {
            Page<FailedAuditLog> failedLogsPage = failedAuditLogRepository.findAll(PageRequest.of(currentPage, PAGE_SIZE));

            if (failedLogsPage.isEmpty()) {
                break; // Stop if no more records
            }

            for (FailedAuditLog log : failedLogsPage.getContent()) {
                try {
                    AuditLog auditLog = convertToAuditLog(log);
                    auditLogProducer.logToKafka(auditLog);

                    // Delete only if Kafka sent the message successfully
                    failedAuditLogRepository.delete(log);
                    logger.info("Successfully retried and sent failed audit log: {}", log.getId());

                } catch (Exception e) {
                    logger.error("Retrying failed audit log failed again for ID: {}, reason: {}",
                            log.getId(), log.getFailureReason(), e);
                }
            }

            currentPage++; // Move to the next batch
        }
    }
    private AuditLog convertToAuditLog(FailedAuditLog failedLog) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(failedLog.getAction());
        auditLog.setUserName(failedLog.getUserName());
        auditLog.setDeviceDetails(failedLog.getDeviceDetails());
        auditLog.setTimestamp(failedLog.getTimestamp());
        auditLog.setNewValue(failedLog.getNewValue());
        auditLog.setOldValue(failedLog.getOldValue());
        auditLog.setSignature(failedLog.getSignature());
        return auditLog;
    }
}
