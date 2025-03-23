package com.ridhitek.audit.service;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        Page<AuditLog> page = new PageImpl<>(logs);

        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<AuditLog> retrievedLogs = auditService.getAllAuditLogs(0, 10, "id", "asc");
        assertEquals(2, retrievedLogs.getContent().size());
    }

    @Test
    void testGetAuditLogById() {
        AuditLog expectedAuditLog = new AuditLog();
        expectedAuditLog.setId(1L);
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(expectedAuditLog));

        Optional<AuditLog> actualAuditLog = auditService.getAuditLogById(1L);

        assertTrue(actualAuditLog.isPresent(), "Audit log should be present");
        assertEquals(expectedAuditLog, actualAuditLog.get(), "Audit log should match the expected value");
    }

    @Test
    public void testDeleteAuditLog() {
        doNothing().when(auditLogRepository).deleteById(1L);

        auditService.deleteAuditLog(1L);
        verify(auditLogRepository, times(1)).deleteById(1L);
    }
}