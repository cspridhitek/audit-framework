package com.ridhitek.audit.consumer;

import com.ridhitek.audit.entity.AuditLog;
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
        doNothing().when(auditLogRepository).save(any(AuditLog.class));
        auditLogConsumer.consume(auditLog);
        verify(auditLogRepository, times(1)).save(auditLog);
    }

    @Test
    void testConsume_FailureTriggersRetryAndFallback() {
        doThrow(new RuntimeException("Database down"))
                .when(auditLogRepository).save(any(AuditLog.class));

        auditLogConsumer.consume(auditLog);

        verify(auditLogRepository, times(3)).save(any(AuditLog.class)); // Ensuring retry
        verify(failedAuditLogRepository, times(1)).save(any()); // Ensuring fallback
    }
}
