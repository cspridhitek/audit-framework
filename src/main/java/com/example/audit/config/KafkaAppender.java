package com.example.audit.config;

import com.example.audit.entity.AuditLog;
import com.example.audit.entity.FailedAuditLog;
import com.example.audit.service.AuditService;
import com.example.audit.repository.FailedAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class KafkaAppender {
    private static final Logger logger = LoggerFactory.getLogger(KafkaAppender.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuditService auditService;
    private final FailedAuditLogRepository failedAuditLogRepository;

    @Value("${retry.maxAttempts:3}")
    private int maxAttempts;

    @Value("${retry.backoff.delay:1000}")
    private long backoffDelay;

    @Value("${kafka.topic.name:default-audit-topic}")
    private String auditTopic;

    @Autowired
    public KafkaAppender(KafkaTemplate<String, String> kafkaTemplate, AuditService auditService, FailedAuditLogRepository failedAuditLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditService = auditService;
        this.failedAuditLogRepository = failedAuditLogRepository;
    }

    /**
     * Logs an audit message to Kafka with retry mechanism.
     *
     * @param actor   the actor performing the action
     * @param action  the action being performed
     * @param details additional details about the action
     */
    @Retryable(
        value = { Exception.class },
        maxAttemptsExpression = "#{@retryMaxAttempts}",
        backoff = @Backoff(delayExpression = "#{@retryBackoffDelay}")
    )
    public void logToKafka(String actor, String action, String details) {
        AuditLog auditLog = new AuditLog(action, "", details, actor);
        String message = auditLog.toString();
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(auditTopic, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                // Save the audit log to the database on successful Kafka send
                auditService.saveAuditLog(auditLog);
            }

            @Override
            public void onFailure(Throwable ex) {
                // Log the error and save the failed audit log
                logger.error("Failed to log audit: {}", ex.getMessage(), ex);
                saveFailedAuditLog(actor, action, details, ex.getMessage());
                // Optionally, rethrow the exception to trigger retry
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Saves a failed audit log to the database.
     *
     * @param actor        the actor performing the action
     * @param action       the action being performed
     * @param details      additional details about the action
     * @param errorMessage the error message encountered during logging
     */
    public void saveFailedAuditLog(String actor, String action, String details, String errorMessage) {
        try {
            FailedAuditLog failedAuditLog = new FailedAuditLog(actor, action, details, errorMessage, LocalDateTime.now());
            failedAuditLogRepository.save(failedAuditLog);
        } catch (Exception e) {
            // Log the error if saving the failed audit log also fails
            logger.error("Failed to save failed audit log: {}", e.getMessage(), e);
        }
    }
}