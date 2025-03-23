package com.ridhitek.audit.consumer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogConsumerTest {

    @Mock
    private FailedAuditLogRepository failedAuditLogRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogConsumer auditLogConsumer;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setUserName("testUser");
    }

    @Test
    void testConsume_SuccessfulSave() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        auditLogConsumer.consume(auditLog);
        verify(auditLogRepository, times(1)).save(auditLog);
        verify(failedAuditLogRepository, never()).save(any(FailedAuditLog.class));
    }

    @Test
    void testConsume_FailureTriggersRetryAndFallback() {
        RuntimeException dbException = new RuntimeException("Database down");
        when(auditLogRepository.save(any(AuditLog.class)))
            .thenThrow(dbException);

        auditLogConsumer.consume(auditLog);

        // Verify that save was attempted once (no retries in test)
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        // Verify that the failed audit log was saved
        verify(failedAuditLogRepository, times(1)).save(any(FailedAuditLog.class));
    }
}
