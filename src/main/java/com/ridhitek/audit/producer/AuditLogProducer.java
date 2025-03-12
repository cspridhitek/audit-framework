package com.ridhitek.audit.producer;


import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import com.ridhitek.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.CompletableFuture;

@EnableRetry
//@ConditionalOnProperty(name = "audit.handler-type", havingValue = "kafka_database")
@Service
public class AuditLogProducer {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogProducer.class);

    private final KafkaTemplate<String, AuditLog> kafkaTemplate;
    private final AuditService auditService;
    private final FailedAuditLogRepository failedAuditLogRepository;

    @Value("${retry.maxAttempts:3}")
    private int maxAttempts;

    @Value("${retry.backoff.delay:1000}")
    private long backoffDelay;

    @Value("${spring.kafka.topic}")
    private String auditTopic;

    public AuditLogProducer(KafkaTemplate<String, AuditLog> kafkaTemplate, AuditService auditService, FailedAuditLogRepository failedAuditLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditService = auditService;
        this.failedAuditLogRepository = failedAuditLogRepository;
    }

    /**
     * Logs an audit message to Kafka with retry mechanism.
     */
    @Retryable(
            retryFor = {Exception.class},
            maxAttemptsExpression = "3",
            backoff = @Backoff(delayExpression = "2000")
    )
    public void logToKafka(AuditLog auditLog) {
        try {
            CompletableFuture<SendResult<String, AuditLog>> future = kafkaTemplate.send(auditTopic, auditLog);

            future.handle((result, ex) -> {
                if (ex != null) {
                    System.err.println("Message sending failed: " + auditLog + ", Error: " + ex.getMessage());
                    saveFailedAuditLog(auditLog, ex.getMessage());
                } else {
                    System.out.println("Message sent successfully to partition: " + result.getRecordMetadata().partition());
                }
                return null;
            });

        } catch (Exception e) {
            System.err.println("Exception while sending Kafka message: " + e.getCause());
            saveFailedAuditLog(auditLog, e.getCause().getMessage());
        }

    }

    /**
     * Saves a failed audit log to the database.
     */
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
