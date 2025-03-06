package com.ridhitek.audit.producer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import com.ridhitek.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogProducerTest {

    @Mock
    private KafkaTemplate<String, AuditLog> kafkaTemplate;

    @Mock
    private AuditService auditService;

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    @InjectMocks
    private AuditLogProducer auditLogProducer;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setUserName("testUser");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDeviceDetails("127.0.0.1");
        auditLog.setNewValue("new data");
        auditLog.setOldValue("old data");
        auditLog.setSignature("signature");
    }

    @Test
    void testLogToKafka_Success() {
        // Mock successful Kafka message sending
        CompletableFuture<SendResult<String, AuditLog>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(future);

        // Call the method
        auditLogProducer.logToKafka(auditLog);

        // Verify Kafka template was used
        verify(kafkaTemplate, times(1)).send(anyString(), any(AuditLog.class));

        // Ensure failedAuditLogRepository was not used
        verify(failedAuditLogRepository, never()).save(any(FailedAuditLog.class));
    }

    @Test
    void testLogToKafka_Failure() {
        // Mock Kafka failure
        CompletableFuture<SendResult<String, AuditLog>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(future);

        // Call the method
        auditLogProducer.logToKafka(auditLog);

        // Verify that failed audit log is saved
        verify(failedAuditLogRepository, times(1)).save(any(FailedAuditLog.class));
    }
}
