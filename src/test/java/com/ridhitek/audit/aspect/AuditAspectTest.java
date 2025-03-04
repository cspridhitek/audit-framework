package com.ridhitek.audit.aspect;

import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.service.AuditService;
import com.ridhitek.audit.util.DigitalSignatureUtil;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.aspectj.lang.Signature;

@Aspect
public class AuditAspectTest {

    @Mock
    private AuditService auditService;

//    @InjectMocks
//    private AuditAspect auditAspect;

    public AuditAspectTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Pointcut("@annotation(com.example.audit.annotation.Auditable)")
    public void auditableMethods() {
    }

    @AfterReturning(pointcut = "auditableMethods()", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {

        LocalDateTime timestamp =  LocalDateTime.now();
        String action = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String oldValue = ""; // Logic to retrieve old value
        String newValue = result != null ? result.toString() : null;
        String changedBy = "system"; // Logic to retrieve the user who made the change
//        String signature = DigitalSignatureUtil.signLog(action, changedBy, timestamp.toString());

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUserName(changedBy);
        auditLog.setNewValue(newValue);
        auditLog.setOldValue(oldValue);
        auditLog.setSignature(null);
        auditService.saveAuditLog(auditLog);
    }

    @Test
    public void testLogAudit() {
        JoinPoint joinPoint = mock(JoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testAction");
        when(joinPoint.getArgs()).thenReturn(new Object[] {});

        // auditAspect.logAudit(joinPoint, "newValue");

        verify(auditService, times(1)).saveAuditLog(any(AuditLog.class));
    }
}