package com.ridhitek.audit.consumer;


import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@EnableRetry
@Component
public class AuditLogConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogConsumer.class);


//    @Value("${spring.kafka.topic}")
//    private String auditTopic;
//
//    @Value("${spring.kafka.consumer.group-id}")
//    private String auditGroupId;

    @Value("${retry.maxAttempts}")
    private int retryMaxAttempts;

    @Value("${retry.backoff.delay}")
    private long retryBackoffDelay;

    private final FailedAuditLogRepository failedAuditLogRepository;

    private final AuditLogRepository auditLogRepository;

    public AuditLogConsumer(FailedAuditLogRepository failedAuditLogRepository, AuditLogRepository auditLogRepository) {
        this.failedAuditLogRepository = failedAuditLogRepository;
        this.auditLogRepository = auditLogRepository;
    }



    @KafkaListener( topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Retryable(
            value = { RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void consume(AuditLogEntity auditLogEntity) {
        logger.info("Storing record in DB...");
        auditLogRepository.save(auditLogEntity);
    }

    @Recover
    public void recover(Exception e, String message) {
        System.out.println("Failed after retries, applying fallback: " + message);
        saveFailedAuditLog(message, e.getMessage());
    }

    private void saveFailedAuditLog(String message, String error) {
        System.out.println("Saving to fallback storage: " + message + " | Error: " + error);
        // Store failed logs in DB or send to a Dead Letter Queue
        FailedAuditLog log = new FailedAuditLog();
//        log.setMessage(message);
        log.setErrorMessage(error);
        failedAuditLogRepository.save(log);
    }
}

