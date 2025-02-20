package com.ridhitek.audit.controller;

import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.service.AuditService;
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
        AuditLogEntity log1 = new AuditLogEntity();
        AuditLogEntity log2 = new AuditLogEntity();
        List<AuditLogEntity> logs = Arrays.asList(log1, log2);

        when(auditService.getAllAuditLogs()).thenReturn(logs);

        ResponseEntity<List<AuditLogEntity>> response = auditController.getAllAuditLogs();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testGetAuditLogById() {
        
        AuditLogEntity log = new AuditLogEntity();
        when(auditService.getAuditLogById(1L)).thenReturn(log);

        ResponseEntity<AuditLogEntity> response = auditController.getAuditLogById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(log, response.getBody());
    }

    @Test
    public void testCreateAuditLog() {
        AuditLogEntity log = new AuditLogEntity();
        when(auditService.saveAuditLog(log)).thenReturn(log);

        ResponseEntity<AuditLogEntity> response = auditController.createAuditLog(log);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(log, response.getBody());
    }
}