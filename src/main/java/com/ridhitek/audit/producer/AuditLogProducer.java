package com.ridhitek.audit.producer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import com.ridhitek.audit.service.AuditService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.kafka.common.KafkaException;
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
import java.util.concurrent.TimeUnit;

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

    @Value("${kafka.send.timeout:5000}")
    private long kafkaSendTimeout;

    public AuditLogProducer(KafkaTemplate<String, AuditLog> kafkaTemplate, AuditService auditService, FailedAuditLogRepository failedAuditLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditService = auditService;
        this.failedAuditLogRepository = failedAuditLogRepository;
    }

    @Retryable(
        value = {KafkaException.class},
        maxAttemptsExpression = "#{${retry.maxAttempts:3}}",
        backoff = @Backoff(delayExpression = "#{${retry.backoff.delay:2000}}")
    )
    @CircuitBreaker(name = "auditLogProducer", fallbackMethod = "fallbackLogToKafka")
    public void logToKafka(AuditLog auditLog) {
        if (!validateAuditLog(auditLog)) {
            logger.error("Invalid audit log: {}", auditLog);
            return;
        }

        try {
            CompletableFuture<SendResult<String, AuditLog>> future = kafkaTemplate.send(auditTopic, auditLog);

            // Add timeout to the future
            future.completeOnTimeout(null, kafkaSendTimeout, TimeUnit.MILLISECONDS)
                .whenComplete((result, ex) -> {
                    if (ex != null || result == null) {
                        String errorMessage = ex != null ? ex.getMessage() : "Kafka send timeout";
                        logger.error("Message sending failed: {}, Error: {}", auditLog, errorMessage);
                        saveFailedAuditLog(auditLog, errorMessage);
                    } else {
                        logger.info("Message sent successfully to partition: {}", result.getRecordMetadata().partition());
                    }
                });

            // Wait for the future to complete
            try {
                future.get(kafkaSendTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                // Timeout or other error occurred, but we've already handled it in the whenComplete
                // No need to throw, as the message is saved to failed audit log
                logger.warn("Kafka send operation did not complete: {}", e.getMessage());
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("Exception while sending Kafka message: {}", errorMessage);
            saveFailedAuditLog(auditLog, errorMessage);
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
            logger.error("Failed to save failed audit log: {}", e.getMessage(), e);
        }
    }
}