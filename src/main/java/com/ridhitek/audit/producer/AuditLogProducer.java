package com.ridhitek.audit.producer;


import com.ridhitek.audit.entity.AuditLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(KafkaTemplate.class)
public class AuditLogProducer {

     @Value("${spring.kafka.topic}")
     private String kafkaTopic;

    private final KafkaTemplate<String, AuditLog> kafkaTemplate;

    public AuditLogProducer(KafkaTemplate<String, AuditLog> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAuditLog(AuditLog message) {
        kafkaTemplate.send(kafkaTopic, message);
    }
}
