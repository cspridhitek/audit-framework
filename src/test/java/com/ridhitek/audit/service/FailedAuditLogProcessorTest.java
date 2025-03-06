package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailedAuditLogProcessorTest {

    @Mock
    private AuditLogProducer auditLogProducer;

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    @InjectMocks
    private FailedAuditLogProcessor failedAuditLogProcessor;

    private FailedAuditLog failedLog1, failedLog2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        failedLog1 = new FailedAuditLog();
        failedLog1.setId(1L);
        failedLog1.setAction("CREATE");
        failedLog1.setUserName("testUser1");
        failedLog1.setDeviceDetails("127.0.0.1");
        failedLog1.setTimestamp(LocalDateTime.now());
        failedLog1.setNewValue("new data");
        failedLog1.setOldValue("old data");
        failedLog1.setSignature("signature");
        failedLog1.setFailureReason("Kafka down");

        failedLog2 = new FailedAuditLog();
        failedLog2.setId(2L);
        failedLog2.setAction("UPDATE");
        failedLog2.setUserName("testUser2");
        failedLog2.setDeviceDetails("192.168.1.1");
        failedLog2.setTimestamp(LocalDateTime.now());
        failedLog2.setNewValue("updated data");
        failedLog2.setOldValue("old update");
        failedLog2.setSignature("signature2");
        failedLog2.setFailureReason("Timeout");
    }

    @Test
    void testRetryFailedLogs_SuccessfulRetry() {
        List<FailedAuditLog> failedLogs = Arrays.asList(failedLog1, failedLog2);

        when(failedAuditLogRepository.findAll()).thenReturn(failedLogs);
        doNothing().when(auditLogProducer).logToKafka(any(AuditLog.class));
        doNothing().when(failedAuditLogRepository).delete(any(FailedAuditLog.class));

        failedAuditLogProcessor.retryFailedLogs();

        verify(auditLogProducer, times(2)).logToKafka(any(AuditLog.class));
        verify(failedAuditLogRepository, times(2)).delete(any(FailedAuditLog.class));
    }

    @Test
    void testRetryFailedLogs_FailedRetry() {
        List<FailedAuditLog> failedLogs = Arrays.asList(failedLog1, failedLog2);

        when(failedAuditLogRepository.findAll()).thenReturn(failedLogs);
        doThrow(new RuntimeException("Kafka still down")).when(auditLogProducer).logToKafka(any(AuditLog.class));

        failedAuditLogProcessor.retryFailedLogs();

        verify(auditLogProducer, times(2)).logToKafka(any(AuditLog.class));
        verify(failedAuditLogRepository, never()).delete(any(FailedAuditLog.class));
    }
}
