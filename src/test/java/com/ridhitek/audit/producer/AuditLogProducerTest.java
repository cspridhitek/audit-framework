package com.ridhitek.audit.producer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import com.ridhitek.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AuditLogProducerTest {

    @Mock
    private KafkaTemplate<String, AuditLog> kafkaTemplate;

    @Mock
    private AuditService auditService;

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    private AuditLogProducer auditLogProducer;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditLogProducer = new AuditLogProducer(kafkaTemplate, auditService, failedAuditLogRepository);
        
        // Set required properties
        ReflectionTestUtils.setField(auditLogProducer, "auditTopic", "audit_topic_test");
        
        when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(failedAuditLogRepository.save(any(FailedAuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
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

        // Ensure failedAuditLogRepository was not used - we need to wait for the async completion
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
        
        // Since the future is handled asynchronously, we need to give it time to complete
        try {
            Thread.sleep(100); // Give the async process time to run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify that failed audit log is saved
        verify(failedAuditLogRepository, times(1)).save(any(FailedAuditLog.class));
    }
}
