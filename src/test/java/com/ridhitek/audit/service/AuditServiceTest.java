package com.ridhitek.audit.service;

import com.ridhitek.audit.dto.AuditLogDTO;
import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.repository.AuditLogRepository;
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
        AuditLogEntity log = new AuditLogEntity();
        when(auditLogRepository.save(log)).thenReturn(log);

        AuditLogEntity savedLog = auditService.saveAuditLog(log);
        assertEquals(log, savedLog);
    }

    @Test
    public void testGetAllAuditLogs() {
        AuditLogEntity log1 = new AuditLogEntity();
        AuditLogEntity log2 = new AuditLogEntity();
        List<AuditLogEntity> logs = Arrays.asList(log1, log2);

        when(auditLogRepository.findAll()).thenReturn(logs);

        List<AuditLogEntity> retrievedLogs = auditService.getAllAuditLogs();
        assertEquals(2, retrievedLogs.size());
    }

    @Test
    public void testGetAuditLogById() {
        AuditLogEntity log = new AuditLogEntity();
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(log));

        AuditLogEntity retrievedLog = auditService.getAuditLogById(1L);
        assertEquals(log, retrievedLog);
    }

    @Test
    public void testDeleteAuditLog() {
        doNothing().when(auditLogRepository).deleteById(1L);

        auditService.deleteAuditLog(1L);
        verify(auditLogRepository, times(1)).deleteById(1L);
    }
}