package com.ridhitek.audit.producer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import com.ridhitek.audit.service.AuditService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.CompletableFuture;

@EnableRetry
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
            maxAttemptsExpression = "#{${retry.maxAttempts}}",
            backoff = @Backoff(delayExpression = "#{${retry.backoff.delay}}")
    )
    @CircuitBreaker(name = "auditLogProducer", fallbackMethod = "fallbackLogToKafka")
    public void logToKafka(AuditLog auditLog) {
        if (!validateAuditLog(auditLog)) {
            logger.error("Invalid audit log: {}", auditLog);
            return;
        }

        try {
            CompletableFuture<SendResult<String, AuditLog>> future = kafkaTemplate.send(auditTopic, auditLog);

            future.handle((result, ex) -> {
                if (ex != null) {
                    logger.error("Message sending failed: " + auditLog + ", Error: " + ex.getMessage());
                    saveFailedAuditLog(auditLog, ex.getMessage());
                } else {
                    logger.info("Message sent successfully to partition: " + result.getRecordMetadata().partition());
                }
                return null;
            });

        } catch (Exception e) {
            logger.error("Exception while sending Kafka message: " + e.getCause());
            saveFailedAuditLog(auditLog, e.getCause().getMessage());
        }
    }

    /**
     * Fallback method for circuit breaker.
     */
    public void fallbackLogToKafka(AuditLog auditLog, Throwable t) {
        logger.error("Circuit breaker triggered for audit log: {}", auditLog, t);
        saveFailedAuditLog(auditLog, t.getMessage());
    }

    /**
     * Validates the audit log before sending it to Kafka.
     */
    private boolean validateAuditLog(AuditLog auditLog) {
        // Add validation logic here (e.g., check for null values, invalid data, etc.)
        return auditLog != null && !ObjectUtils.isEmpty(auditLog.getAction()) && !ObjectUtils.isEmpty(auditLog.getUserName());
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
            logger.info("Failed audit log saved successfully.");
        } catch (Exception e) {
            // Log the error if saving the failed audit log also fails
            logger.error("Failed to save failed audit log: {}", e.getMessage(), e);
        }
    }
}