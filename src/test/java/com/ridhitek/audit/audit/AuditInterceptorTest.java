package com.ridhitek.audit.audit;

import com.ridhitek.audit.config.AuditProperties;
import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.AuditLogRepository;
import org.hibernate.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import java.io.Serializable;
import static org.mockito.Mockito.*;

class AuditInterceptorTest {

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
        when(context.getBean(AuditLogRepository.class)).thenReturn(auditLogRepository);
        when(context.getBean(AuditLogProducer.class)).thenReturn(auditLogProducer);
        when(context.getBean(AuditProperties.class)).thenReturn(auditProperties);
    }

    @Test
    void testOnSave_CreatesAuditLog() {
        Object entity = new Object();
        Serializable id = 1L;
        Object[] state = {"value1"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        when(auditProperties.getHandlerType()).thenReturn("database");
        auditInterceptor.onSave(entity, id, state, propertyNames, types);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testOnFlushDirty_UpdatesAuditLog() {
        Object entity = new Object();
        Serializable id = 1L;
        Object[] currentState = {"newValue"};
        Object[] previousState = {"oldValue"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        when(auditProperties.getHandlerType()).thenReturn("database");
        auditInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testOnDelete_DeletesAuditLog() {
        Object entity = new Object();
        Serializable id = 1L;
        Object[] state = {"value1"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        auditInterceptor.onDelete(entity, id, state, propertyNames, types);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testSaveAuditLog_UsesKafkaWhenConfigured() {
        AuditLog auditLog = new AuditLog();
        when(auditProperties.getHandlerType()).thenReturn("kafka_database");
        auditInterceptor.saveAuditLog(auditLog);
        verify(auditLogProducer, times(1)).logToKafka(auditLog);
        verify(auditLogRepository, never()).save(auditLog);
    }
}

