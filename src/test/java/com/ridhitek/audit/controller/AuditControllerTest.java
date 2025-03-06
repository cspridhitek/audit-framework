package com.ridhitek.audit.controller;

import com.ridhitek.audit.dto.AuditLogDTO;
import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAuditLogs_ReturnsAuditLogs() {
        List<AuditLog> mockAuditLogs = Arrays.asList(
                new AuditLog(),
                new AuditLog()
        );

        when(auditService.getAllAuditLogs()).thenReturn(mockAuditLogs);

        ResponseEntity<List<AuditLogDTO>> response = auditController.getAllAuditLogs();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }
}
