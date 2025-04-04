package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailedAuditLogProcessorTest {

    @Mock
    private AuditLogProducer auditLogProducer;

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    @InjectMocks
    private FailedAuditLogProcessor failedAuditLogProcessor;

    private FailedAuditLog failedLog;

    @BeforeEach
    void setUp() {
        failedLog = new FailedAuditLog();
        failedLog.setId(1L);
        failedLog.setAction("CREATE");
        failedLog.setUserName("testUser");
        failedLog.setDeviceDetails("Windows 10");
        failedLog.setTimestamp(LocalDateTime.now());
        failedLog.setNewValue("{\"name\": \"newValue\"}");
        failedLog.setOldValue("{\"name\": \"oldValue\"}");
        failedLog.setSignature("signature123");
    }

    @Test
    void retryFailedLogs_NoLogsToProcess() {
        when(failedAuditLogRepository.count()).thenReturn(0L);

        failedAuditLogProcessor.retryFailedLogs();

        verify(failedAuditLogRepository, never()).findAll(any(PageRequest.class));
        verify(auditLogProducer, never()).logToKafka(any(AuditLog.class));
    }

    @Test
    void retryFailedLogs_SuccessfullyRetriesAndDeletesLogs() {
        Page<FailedAuditLog> failedLogsPage = new PageImpl<>(List.of(failedLog));
        when(failedAuditLogRepository.count()).thenReturn(1L).thenReturn(0L); // Stops loop after first run
        when(failedAuditLogRepository.findAll(any(PageRequest.class)))
                .thenReturn(failedLogsPage)
                .thenReturn(Page.empty()); // Stops loop

        failedAuditLogProcessor.retryFailedLogs();

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogProducer, times(1)).logToKafka(auditLogCaptor.capture());
        verify(failedAuditLogRepository, times(1)).delete(failedLog);

        AuditLog sentAuditLog = auditLogCaptor.getValue();
        assertNotNull(sentAuditLog);
        assertEquals("CREATE", sentAuditLog.getAction());
        assertEquals("testUser", sentAuditLog.getUserName());
    }

    @Test
    void retryFailedLogs_FailedLogsAreNotDeletedOnKafkaFailure() {
        Page<FailedAuditLog> failedLogsPage = new PageImpl<>(List.of(failedLog));
        when(failedAuditLogRepository.count()).thenReturn(1L).thenReturn(0L); // Stops loop
        when(failedAuditLogRepository.findAll(any(PageRequest.class)))
                .thenReturn(failedLogsPage)
                .thenReturn(Page.empty()); // Stops loop
        doThrow(new RuntimeException("Kafka failure"))
                .when(auditLogProducer).logToKafka(any(AuditLog.class));

        failedAuditLogProcessor.retryFailedLogs();

        verify(failedAuditLogRepository, never()).delete(any(FailedAuditLog.class));
    }
}

