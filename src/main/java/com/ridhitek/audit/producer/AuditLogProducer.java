package com.ridhitek.audit.producer;


import com.ridhitek.audit.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;

@Service
public class AuditLogProducer {

     @Value("${spring.kafka.topic}")
     private String kafkaTopic;

    private final KafkaTemplate<String, AuditLogEntity> kafkaTemplate;

    public AuditLogProducer(KafkaTemplate<String, AuditLogEntity> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAuditLog(AuditLogEntity message) {
        kafkaTemplate.send(kafkaTopic, message);
    }
}
