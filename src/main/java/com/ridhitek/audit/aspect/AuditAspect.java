package com.ridhitek.audit.aspect;

import java.time.LocalDateTime;

import com.ridhitek.audit.entity.Employee;
import com.ridhitek.audit.repository.EmployeeRepository;
import com.ridhitek.audit.service.EmployeeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ridhitek.audit.annotation.Auditable;
import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.util.DigitalSignatureUtil;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LogManager.getLogger(AuditAspect.class);

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private HttpServletRequest request; // For capturing IP & User details

    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result;
        Long employeeId = null;
        Employee oldEmployee = null;
        Employee newEmployee = null;
        String action = auditable.action().isEmpty() ? joinPoint.getSignature().getName() : auditable.action();

        // Capture method arguments (Before execution)
        Object[] args = joinPoint.getArgs();

        // Extract ID from Employee argument
        for (Object arg : args) {
            if (arg instanceof Employee) {
                newEmployee = (Employee) arg;
                employeeId = ((Employee) arg).getId(); // Extract ID
                break;
            }
        }
        if (employeeId != null) {
            oldEmployee = employeeService.getEmployeeById(employeeId);
        }

        // Execute method
        result = joinPoint.proceed();

        // Capture user & request details
        String changedBy = "Nirmala";
        String ipAddress = request.getRemoteAddr();

        // Create and save audit log
        AuditLogEntity audit = new AuditLogEntity();
        audit.setAction(action);
        audit.setOldValue(String.valueOf(oldEmployee));
        audit.setNewValue(newEmployee != null ? String.valueOf(newEmployee) : null);
        audit.setUserName(changedBy);
        audit.setDeviceDetails(ipAddress);
        LocalDateTime timeStamp = LocalDateTime.now();
        audit.setTimestamp(timeStamp);
        audit.setSignature(DigitalSignatureUtil.signLog(action, changedBy, timeStamp.toString()));
        auditLogRepository.save(audit);

        return result;
    }
}

