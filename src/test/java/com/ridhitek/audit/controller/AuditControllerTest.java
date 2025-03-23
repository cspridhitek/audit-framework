package com.ridhitek.audit.controller;

import com.ridhitek.audit.dto.AuditLogDTO;
import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
        // Create mock audit logs
        AuditLog log1 = new AuditLog();
        log1.setUserName("user1");
        log1.setAction("CREATE");
        log1.setTimestamp(LocalDateTime.now());
        
        AuditLog log2 = new AuditLog();
        log2.setUserName("user2");
        log2.setAction("UPDATE");
        log2.setTimestamp(LocalDateTime.now());
        
        List<AuditLog> mockAuditLogs = Arrays.asList(log1, log2);
        Page<AuditLog> mockPage = new PageImpl<>(mockAuditLogs);

        when(auditService.getAllAuditLogs(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockPage);

        ResponseEntity<Page<AuditLogDTO>> response = auditController.getAllAuditLogs(0, 10, "timestamp", "desc");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
    }
}
