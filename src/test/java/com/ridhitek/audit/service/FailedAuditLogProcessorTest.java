package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FailedAuditLogProcessorTest {

    @MockBean
    private AuditLogProducer auditLogProducer;

    @MockBean
    private FailedAuditLogRepository failedAuditLogRepository;

    @Autowired
    private FailedAuditLogProcessor failedAuditLogProcessor;

    private FailedAuditLog failedLog1, failedLog2;

    @BeforeEach
    void setUp() {
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

        // Reset interactions before each test
        reset(auditLogProducer, failedAuditLogRepository);
    }

    @Test
    void testRetryFailedLogs_SuccessfulRetry() {
        // Set up test data
        List<FailedAuditLog> failedLogs = Arrays.asList(failedLog1, failedLog2);
        
        // Configure mocks
        when(failedAuditLogRepository.findAll()).thenReturn(failedLogs);
        
        // Call the method under test
        failedAuditLogProcessor.retryFailedLogs();
        
        // Verify that logToKafka was called exactly twice (once for each failed log)
        verify(auditLogProducer, times(2)).logToKafka(any(AuditLog.class));
        
        // Verify that delete was called exactly twice (once for each failed log)
        verify(failedAuditLogRepository, times(2)).delete(any(FailedAuditLog.class));
        
        // Capture and verify the audit logs
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogProducer, times(2)).logToKafka(auditLogCaptor.capture());
        
        List<AuditLog> capturedLogs = auditLogCaptor.getAllValues();
        assertEquals(2, capturedLogs.size());
        
        // Verify first captured log
        AuditLog firstLog = capturedLogs.get(0);
        assertEquals("CREATE", firstLog.getAction());
        assertEquals("testUser1", firstLog.getUserName());
        
        // Verify second captured log
        AuditLog secondLog = capturedLogs.get(1);
        assertEquals("UPDATE", secondLog.getAction());
        assertEquals("testUser2", secondLog.getUserName());
    }

    @Test
    void testRetryFailedLogs_FailedRetry() {
        // Set up test data
        List<FailedAuditLog> failedLogs = Arrays.asList(failedLog1, failedLog2);
        
        // Configure mocks
        when(failedAuditLogRepository.findAll()).thenReturn(failedLogs);
        
        // Configure auditLogProducer to throw exception when logToKafka is called
        doThrow(new RuntimeException("Kafka still down")).when(auditLogProducer).logToKafka(any(AuditLog.class));
        
        // Call the method under test
        failedAuditLogProcessor.retryFailedLogs();
        
        // Verify that logToKafka was called exactly twice (once for each failed log)
        verify(auditLogProducer, times(2)).logToKafka(any(AuditLog.class));
        
        // Verify that delete was never called since the retry failed
        verify(failedAuditLogRepository, never()).delete(any(FailedAuditLog.class));
    }
}
