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
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
        
        // Configure the default audit type for tests
        when(auditProperties.getHandlerType()).thenReturn("database");
        
        // Mock the repository save method to return the entity
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testOnSave_CreatesAuditLog() throws InterruptedException {
        // Setup test data
        Object entity = new TestEntity(); // Using a test entity instead of AuditLog
        Serializable id = 1L;
        Object[] state = {"value1"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        // Execute the method under test
        auditInterceptor.onSave(entity, id, state, propertyNames, types);

        // Wait for the async task to complete
        TimeUnit.SECONDS.sleep(1);

        // Verify repository was called with correct data
        verify(auditLogRepository, timeout(5000).times(1)).save(any(AuditLog.class));
    }

    @Test
    void testOnFlushDirty_UpdatesAuditLog() throws InterruptedException {
        // Setup test data
        Object entity = new TestEntity(); // Using a test entity instead of AuditLog
        Serializable id = 1L;
        Object[] currentState = {"newValue"};
        Object[] previousState = {"oldValue"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        // Execute the method under test
        auditInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);

        // Wait for the async task to complete
        TimeUnit.SECONDS.sleep(1);

        // Verify repository was called with correct data
        verify(auditLogRepository, timeout(5000).times(1)).save(any(AuditLog.class));
    }

    @Test
    void testOnDelete_DeletesAuditLog() throws InterruptedException {
        // Setup test data
        Object entity = new TestEntity(); // Using a test entity instead of AuditLog
        Serializable id = 1L;
        Object[] state = {"value1"};
        String[] propertyNames = {"field1"};
        Type[] types = new Type[1];

        // Execute the method under test
        auditInterceptor.onDelete(entity, id, state, propertyNames, types);

        // Wait for the async task to complete
        TimeUnit.SECONDS.sleep(1);

        // Verify repository was called with correct data
        verify(auditLogRepository, timeout(5000).times(1)).save(any(AuditLog.class));
    }

    @Test
    void testSaveAuditLog_UsesKafkaWhenConfigured() {
        AuditLog auditLog = new AuditLog();
        when(auditProperties.getHandlerType()).thenReturn("kafka_database");
        auditInterceptor.saveAuditLog(auditLog);
        verify(auditLogProducer, times(1)).logToKafka(auditLog);
        verify(auditLogRepository, never()).save(auditLog);
    }
    
    // Test entity class for use in tests
    static class TestEntity {
        private String field1;
        
        public String getField1() {
            return field1;
        }
        
        public void setField1(String field1) {
            this.field1 = field1;
        }
    }
}

