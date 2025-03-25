package com.ridhitek.audit.consumer;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.repository.FailedAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AuditLogConsumerTest {

    @MockBean
    private FailedAuditLogRepository failedAuditLogRepository;

    @MockBean
    private AuditLogRepository auditLogRepository;

    private AuditLogConsumer auditLogConsumer;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLogConsumer = new AuditLogConsumer(failedAuditLogRepository, auditLogRepository);
        
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(failedAuditLogRepository.save(any(FailedAuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setUserName("testUser");
    }

    @Test
    void testConsume_SuccessfulSave() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        auditLogConsumer.consume(auditLog);
        verify(auditLogRepository, times(1)).save(auditLog);
    }

    @Test
    void testConsume_FailureTriggersRetryAndFallback() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("Database down"));

        auditLogConsumer.consume(auditLog);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class)); 
        verify(failedAuditLogRepository, times(1)).save(any(FailedAuditLog.class));
    }
}
