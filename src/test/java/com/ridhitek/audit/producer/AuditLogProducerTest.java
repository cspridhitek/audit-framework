package com.ridhitek.audit.producer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import com.ridhitek.audit.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogProducerTest {

    @Mock
    private KafkaTemplate<String, AuditLog> kafkaTemplate;

    @Mock
    private AuditService auditService;

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogProducer auditLogProducer;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        System.setProperty("AUDIT_LOG_SECRET_KEY", "test-secret-key");
        ReflectionTestUtils.setField(auditLogProducer, "auditTopic", "test-topic");
        ReflectionTestUtils.setField(auditLogProducer, "maxAttempts", 3);
        ReflectionTestUtils.setField(auditLogProducer, "backoffDelay", 100L);
        
        auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setUserName("testUser");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDeviceDetails("127.0.0.1");
        auditLog.setNewValue("new data");
        auditLog.setOldValue("old data");
        auditLog.setSignature("signature");

        lenient().when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());
        lenient().when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void testLogToKafka_Success() {
        SendResult<String, AuditLog> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, AuditLog>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(future);

        assertDoesNotThrow(() -> auditLogProducer.logToKafka(auditLog));

        verify(kafkaTemplate, times(1)).send(anyString(), any(AuditLog.class));
        verify(failedAuditLogRepository, never()).save(any(FailedAuditLog.class));
    }

    @Test
    void testLogToKafka_Failure() {
        // Simulate Kafka being down
        CompletableFuture<SendResult<String, AuditLog>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(future);

        // Mock successful save to fallback storage
        FailedAuditLog expectedFailedLog = new FailedAuditLog();
        when(failedAuditLogRepository.save(any(FailedAuditLog.class))).thenReturn(expectedFailedLog);

        // Execute and verify no exceptions are thrown
        assertDoesNotThrow(() -> auditLogProducer.logToKafka(auditLog));

        // Verify Kafka was attempted
        verify(kafkaTemplate, times(1)).send(anyString(), any(AuditLog.class));

        // Verify fallback storage was used
        ArgumentCaptor<FailedAuditLog> failedLogCaptor = ArgumentCaptor.forClass(FailedAuditLog.class);
        verify(failedAuditLogRepository, times(1)).save(failedLogCaptor.capture());
        
        FailedAuditLog capturedLog = failedLogCaptor.getValue();
        assertEquals(auditLog.getAction(), capturedLog.getAction());
        assertEquals(auditLog.getUserName(), capturedLog.getUserName());
        assertTrue(capturedLog.getFailureReason().contains("Kafka error"));
    }

    @Test
    void testCircuitBreaker_Fallback() {
        RuntimeException exception = new RuntimeException("Circuit open");
        FailedAuditLog savedLog = new FailedAuditLog();
        when(failedAuditLogRepository.save(any(FailedAuditLog.class))).thenReturn(savedLog);
        
        assertDoesNotThrow(() -> auditLogProducer.fallbackLogToKafka(auditLog, exception));
        
        verify(failedAuditLogRepository, times(1)).save(argThat(log -> 
            log.getAction().equals(auditLog.getAction()) &&
            log.getUserName().equals(auditLog.getUserName()) &&
            log.getFailureReason().equals("Circuit open")
        ));
    }

    @Test
    void testLogToKafka_SaveFailedLogError() {
        // Simulate Kafka being down
        CompletableFuture<SendResult<String, AuditLog>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), any(AuditLog.class))).thenReturn(future);

        // Simulate failure to save to fallback storage
        when(failedAuditLogRepository.save(any(FailedAuditLog.class)))
            .thenThrow(new RuntimeException("DB error"));

        // Even with both Kafka and DB failures, the application should not throw
        assertDoesNotThrow(() -> auditLogProducer.logToKafka(auditLog));

        // Verify attempts were made
        verify(kafkaTemplate, times(1)).send(anyString(), any(AuditLog.class));
        verify(failedAuditLogRepository, times(1)).save(any(FailedAuditLog.class));
    }

    @Test
    void testLogToKafka_ValidationFailure() {
        auditLog.setAction(null); // Invalid state
        
        assertDoesNotThrow(() -> auditLogProducer.logToKafka(auditLog));
        
        verify(kafkaTemplate, never()).send(anyString(), any(AuditLog.class));
        verify(failedAuditLogRepository, never()).save(any(FailedAuditLog.class));
    }

    @Test
    void testLogToKafka_KafkaTimeout() {
        // Arrange
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setUserName("testUser");
        auditLog.setNewValue("new data");
        auditLog.setOldValue("old data");
        auditLog.setDeviceDetails("127.0.0.1");
        auditLog.setSignature("signature");
        auditLog.setTimestamp(LocalDateTime.now());

        doAnswer(invocation -> {
            FailedAuditLog failedAuditLog = invocation.getArgument(0);
            assertNotNull(failedAuditLog);
            assertEquals("CREATE", failedAuditLog.getAction());
            assertEquals("testUser", failedAuditLog.getUserName());
            return null;
        }).when(failedAuditLogRepository).save(any(FailedAuditLog.class));

        // Act
        auditLogProducer.logToKafka(auditLog);

        // Assert
        verify(failedAuditLogRepository, times(1)).save(any(FailedAuditLog.class));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("AUDIT_LOG_SECRET_KEY");
    }
}
