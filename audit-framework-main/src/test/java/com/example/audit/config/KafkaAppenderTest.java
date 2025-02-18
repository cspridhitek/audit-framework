package com.example.audit.config;

import com.example.audit.entity.AuditLog;
import com.example.audit.entity.FailedAuditLog;
import com.example.audit.repository.FailedAuditLogRepository;
import com.example.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaAppenderTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private AuditService auditService;

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    @Mock
    private ListenableFuture<SendResult<String, String>> future;

    @InjectMocks
    private KafkaAppender kafkaAppender;

    @Captor
    private ArgumentCaptor<FailedAuditLog> failedAuditLogCaptor;

    @BeforeEach
    void setUp() {
        // Ensure auditTopic is not null
        ReflectionTestUtils.setField(kafkaAppender, "auditTopic", "test-audit-topic");
        ReflectionTestUtils.setField(kafkaAppender, "maxAttempts", 3);
        ReflectionTestUtils.setField(kafkaAppender, "backoffDelay", 1000L);

        // Mock Kafka send behavior
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);
    }

    @Test
    void testLogToKafka_Success() {
        // Arrange
        String actor = "User1";
        String action = "CREATE";
        String details = "Created a record";

        // Mocking callback behavior for success
        doAnswer(invocation -> {
            ListenableFutureCallback<SendResult<String, String>> callback = invocation.getArgument(0);
            callback.onSuccess(mock(SendResult.class));
            return null;
        }).when(future).addCallback(any());

        // Act
        kafkaAppender.logToKafka(actor, action, details);

        // Verify that audit log is saved upon successful Kafka send
        verify(auditService, times(1)).saveAuditLog(any(AuditLog.class));
    }

    @Test
    void testLogToKafka_Failure() {
        // Arrange
        String actor = "User1";
        String action = "DELETE";
        String details = "Deleted a record";
        String errorMessage = "Kafka error";

        // Ensure send() still returns a future
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // Mocking failure callback behavior
        doAnswer(invocation -> {
            ListenableFutureCallback<SendResult<String, String>> callback = invocation.getArgument(0);
            callback.onFailure(new RuntimeException(errorMessage));
            return null;
        }).when(future).addCallback(any());

        // Act
        try {
            kafkaAppender.logToKafka(actor, action, details);
        } catch (Exception ignored) {}

        // Verify that a failed audit log is saved
        verify(failedAuditLogRepository, times(1)).save(failedAuditLogCaptor.capture());

        FailedAuditLog capturedFailedLog = failedAuditLogCaptor.getValue();
        assert capturedFailedLog.getActor().equals(actor);
        assert capturedFailedLog.getAction().equals(action);
        assert capturedFailedLog.getDetails().equals(details);
        assert capturedFailedLog.getErrorMessage().contains(errorMessage);
    }
}
