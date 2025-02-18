package com.example.audit.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
@TestPropertySource(properties = {"audit.handler.type=database"})
public class AuditConfigTest {

    @Autowired
    private AuditConfig auditConfig;

    @MockBean
    private AuditService auditService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    public void setUp() {
        Mockito.when(auditService.saveAuditLog(Mockito.any())).thenReturn(null);
    }

    @Test
    public void testHandlerTypeProperty() {
        Object manager = auditConfig.auditManager();
        assertTrue(manager instanceof DatabaseAppender);
    }
}