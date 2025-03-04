package com.ridhitek.audit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import com.ridhitek.audit.service.AuditService;
import com.ridhitek.audit.repository.FailedAuditLogRepository;

@Configuration
public class AuditConfig {

    @Value("${audit.handler.type}")
    private String handlerType;

    @Autowired
    private AuditService auditService;

//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private FailedAuditLogRepository failedAuditLogRepository;

    @Bean
    public Object auditManager() {
        if ("database".equals(handlerType)) {
            return new DatabaseAppender(auditService);
        } else if ("kafka".equals(handlerType)) {
            //TODO
            System.out.println("kafka is disabled");
            return new Object();
//            return new KafkaAppender(kafkaTemplate, auditService, failedAuditLogRepository);
        } else {
            throw new IllegalArgumentException("Unsupported handler type: " + handlerType);
        }
    }
}