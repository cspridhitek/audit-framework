package com.ridhitek.audit.consumer;


import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@EnableRetry
@Component
public class AuditLogConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogConsumer.class);


    @Value("${retry.maxAttempts:3}")
    private int retryMaxAttempts;

    @Value("${retry.backoff.delay}")
    private long retryBackoffDelay;

    private final FailedAuditLogRepository failedAuditLogRepository;

    private final AuditLogRepository auditLogRepository;

    public AuditLogConsumer(FailedAuditLogRepository failedAuditLogRepository, AuditLogRepository auditLogRepository) {
        this.failedAuditLogRepository = failedAuditLogRepository;
        this.auditLogRepository = auditLogRepository;
    }


    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Retryable(
            retryFor = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public void consume(AuditLog auditLog) {
        try {
            logger.info("Storing record in DB...");
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            saveFailedAuditLog(auditLog, e.getMessage());
        }
    }

    public void saveFailedAuditLog(AuditLog auditLog, String message) {
        try {
            FailedAuditLog failedAuditLog = new FailedAuditLog();
            failedAuditLog.setAction(auditLog.getAction());
            failedAuditLog.setUserName(auditLog.getUserName());
            failedAuditLog.setSignature(auditLog.getSignature());
            failedAuditLog.setTimestamp(auditLog.getTimestamp());
            failedAuditLog.setDeviceDetails(auditLog.getDeviceDetails());
            failedAuditLog.setNewValue(auditLog.getNewValue());
            failedAuditLog.setOldValue(auditLog.getOldValue());
            failedAuditLog.setFailureReason(message);
            failedAuditLogRepository.save(failedAuditLog);
        } catch (Exception e) {
            // Log the error if saving the failed audit log also fails
            logger.error("Failed to save failed audit log: {}", e.getMessage(), e);
        }
    }

}

