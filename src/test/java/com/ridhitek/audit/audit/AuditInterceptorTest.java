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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import java.io.Serializable;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private AuditInterceptor auditInterceptor;

    @BeforeEach
    void setUp() {
        System.setProperty("AUDIT_LOG_SECRET_KEY", "test-secret-key");
        
        when(context.getBean(AuditProperties.class)).thenReturn(auditProperties);
        when(context.getBean(AuditLogProducer.class)).thenReturn(auditLogProducer);
        when(context.getBean(AuditLogRepository.class)).thenReturn(auditLogRepository);
        when(auditProperties.getHandlerType()).thenReturn("database");
        
        auditInterceptor = new AuditInterceptor(context);
    }

    @Test
    void testOnSave_CreatesAuditLog() {
        TestEntity entity = new TestEntity();
        entity.setField1("test value");
        Serializable id = 1L;
        Object[] state = {entity.getField1()};
        String[] propertyNames = {"field1"};
        Type[] types = {mock(Type.class)};

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());
        
        boolean result = auditInterceptor.onSave(entity, id, state, propertyNames, types);
        assertTrue(result);

        verify(auditLogRepository, times(1)).save(argThat(log -> 
            log.getAction().equals("CREATE") &&
            log.getNewValue().contains("test value")
        ));
    }

    @Test
    void testOnFlushDirty_UpdatesAuditLog() {
        TestEntity entity = new TestEntity();
        entity.setField1("new value");
        Serializable id = 1L;
        Object[] currentState = {entity.getField1()};
        Object[] previousState = {"old value"};
        String[] propertyNames = {"field1"};
        Type[] types = {mock(Type.class)};

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());
        
        boolean result = auditInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        assertTrue(result);

        verify(auditLogRepository, times(1)).save(argThat(log -> 
            log.getAction().equals("UPDATE") &&
            log.getOldValue().contains("old value") &&
            log.getNewValue().contains("new value")
        ));
    }

    @Test
    void testOnDelete_DeletesAuditLog() {
        TestEntity entity = new TestEntity();
        entity.setField1("value to delete");
        Serializable id = 1L;
        Object[] state = {entity.getField1()};
        String[] propertyNames = {"field1"};
        Type[] types = {mock(Type.class)};

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());
        auditInterceptor.onDelete(entity, id, state, propertyNames, types);

        verify(auditLogRepository, times(1)).save(argThat(log -> 
            log.getAction().equals("DELETE") &&
            log.getOldValue().contains("value to delete")
        ));
    }

    @Test
    void testSaveAuditLog_UsesKafkaWhenConfigured() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("TEST");
        auditLog.setUserName("testUser");
        when(auditProperties.getHandlerType()).thenReturn("kafka_database");

        auditInterceptor.saveAuditLog(auditLog);

        verify(auditLogProducer, times(1)).logToKafka(auditLog);
        verify(auditLogRepository, never()).save(auditLog);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("AUDIT_LOG_SECRET_KEY");
    }
}

