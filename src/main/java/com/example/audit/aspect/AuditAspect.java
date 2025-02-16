package com.example.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.audit.entity.AuditLog;
import com.example.audit.service.AuditService;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @Pointcut("@annotation(com.example.audit.annotation.Auditable)")
    public void auditableMethods() {
    }

    @AfterReturning(pointcut = "auditableMethods()", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        String action = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String oldValue = ""; // Logic to retrieve old value
        String newValue = result != null ? result.toString() : null;
        String changedBy = "system"; // Logic to retrieve the user who made the change

        AuditLog auditLog = new AuditLog(action, oldValue, newValue, changedBy);
        auditService.saveAuditLog(auditLog);
    }
}