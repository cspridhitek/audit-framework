package com.example.audit.controller;

import com.example.audit.entity.AuditLog;
import com.example.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    public AuditControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllAuditLogs() {
        AuditLog log1 = new AuditLog("action1", "oldValue1", "newValue1", "user1");
        AuditLog log2 = new AuditLog("action2", "oldValue2", "newValue2", "user2");
        List<AuditLog> logs = Arrays.asList(log1, log2);

        when(auditService.getAllAuditLogs()).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = auditController.getAllAuditLogs();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testGetAuditLogById() {
        AuditLog log = new AuditLog("action", "oldValue", "newValue", "user");
        when(auditService.getAuditLogById(1L)).thenReturn(log);

        ResponseEntity<AuditLog> response = auditController.getAuditLogById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(log, response.getBody());
    }

    @Test
    public void testCreateAuditLog() {
        AuditLog log = new AuditLog("action", "oldValue", "newValue", "user");
        when(auditService.saveAuditLog(log)).thenReturn(log);

        ResponseEntity<AuditLog> response = auditController.createAuditLog(log);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(log, response.getBody());
    }
}