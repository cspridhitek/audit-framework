package com.example.audit.service;

import com.example.audit.entity.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    public AuditServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveAuditLog() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.save(log)).thenReturn(log);

        AuditLog savedLog = auditService.saveAuditLog(log);
        assertEquals(log, savedLog);
    }

    @Test
    public void testGetAllAuditLogs() {
        AuditLog log1 = new AuditLog();
        AuditLog log2 = new AuditLog();
        List<AuditLog> logs = Arrays.asList(log1, log2);

        when(auditLogRepository.findAll()).thenReturn(logs);

        List<AuditLog> retrievedLogs = auditService.getAllAuditLogs();
        assertEquals(2, retrievedLogs.size());
    }

    @Test
    public void testGetAuditLogById() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(log));

        AuditLog retrievedLog = auditService.getAuditLogById(1L);
        assertEquals(log, retrievedLog);
    }

    @Test
    public void testDeleteAuditLog() {
        doNothing().when(auditLogRepository).deleteById(1L);

        auditService.deleteAuditLog(1L);
        verify(auditLogRepository, times(1)).deleteById(1L);
    }
}