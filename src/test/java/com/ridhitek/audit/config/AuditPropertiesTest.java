package com.ridhitek.audit.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AuditProperties.class)
@EnableConfigurationProperties(AuditProperties.class)
@TestPropertySource(properties = {"audit.handlerType=kafka_database"})
class AuditPropertiesTest {

    @Autowired
    private AuditProperties auditProperties;

    @Test
    void testAuditPropertiesBinding() {
        assertNotNull(auditProperties, "AuditProperties bean should be loaded");
        assertEquals("kafka_database", auditProperties.getHandlerType(), "HandlerType should match the configured value");
    }
}
