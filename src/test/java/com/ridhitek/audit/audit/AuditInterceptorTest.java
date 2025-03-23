package com.ridhitek.audit.audit;

import com.ridhitek.audit.config.AuditProperties;
import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.AuditLogRepository;
import org.hibernate.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuditInterceptorTest {

    private static class TestEntity {
        private String field1;
        
        public String getField1() {
            return field1;
        }
        
        public void setField1(String field1) {
            this.field1 = field1;
        }
    }

    @Mock
    private ApplicationContext context;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogProducer auditLogProducer;

    @Mock
    private AuditProperties auditProperties;

    @InjectMocks
    private AuditInterceptor auditInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditInterceptor = new AuditInterceptor(auditLogRepository, auditLogProducer, auditProperties);
    }

    @Test
    void testOnSave_CreatesAuditLog() throws InterruptedException {
        // Arrange
        Object entity = new TestEntity(); // Use TestEntity instead of Object
        Serializable id = 1L;
        Object[] state = {"value1"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        boolean result = auditInterceptor.onSave(entity, id, state, propertyNames, types);

        // Wait for async task to complete
        auditInterceptor.shutdownExecutorService();

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        assertTrue(result);
    }

    @Test
    void testOnFlushDirty_UpdatesAuditLog() throws InterruptedException {
        // Arrange
        Object entity = new TestEntity(); // Use TestEntity instead of Object
        Serializable id = 1L;
        Object[] currentState = {"newValue"};
        Object[] previousState = {"oldValue"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertNotNull(auditLog);
            assertEquals("UPDATE", auditLog.getAction());
            return auditLog;
        });

        // Act
        boolean result = auditInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);

        // Wait for async task to complete
        auditInterceptor.shutdownExecutorService();

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        assertTrue(result);
    }

    @Test
    void testOnDelete_DeletesAuditLog() {
        // Arrange
        Object entity = new Object();
        Serializable id = 1L;
        Object[] state = {"value1"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditInterceptor.onDelete(entity, id, state, propertyNames, types);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testSaveAuditLog_UsesKafkaWhenConfigured() {
        // Arrange
        AuditLog auditLog = new AuditLog();
        when(auditProperties.getHandlerType()).thenReturn("kafka_database");

        // Act
        auditInterceptor.saveAuditLog(auditLog);

        // Assert
        verify(auditLogProducer, times(1)).logToKafka(auditLog);
        verify(auditLogRepository, never()).save(auditLog);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("AUDIT_LOG_SECRET_KEY");
    }
}

