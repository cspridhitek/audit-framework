package com.ridhitek.audit.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridhitek.audit.annotation.Auditable;
import com.ridhitek.audit.entity.AuditLogEntity;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.util.DigitalSignatureUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = Logger.getLogger(AuditAspect.class.getName());

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogProducer auditLogProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action().isEmpty() ? joinPoint.getSignature().getName() : auditable.action();

        Object[] args = joinPoint.getArgs();
        Object targetEntity = null;
        Object oldEntity = null;

        // Extract entity from method arguments
        for (Object arg : args) {
            targetEntity = arg;
            break;
        }

        if (targetEntity == null) {
            return joinPoint.proceed();
        }

        // Fetch the old value for UPDATE and DELETE operations
        Optional<?> existingEntity = getExistingEntity(targetEntity);
        if (existingEntity.isPresent()) {
            oldEntity = existingEntity.get();
        }

        // Proceed with method execution
        Object newEntity = joinPoint.proceed();

        // Convert entities to JSON for auditing
        String oldValueJson = convertToJson(oldEntity);
        String newValueJson = convertToJson(newEntity);

        String changedBy = "SYSTEM"; // Default Name
        String ipAddress = request.getRemoteAddr();

        // Create and save audit log
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setAction(action);
        auditLog.setOldValue(oldValueJson);
        auditLog.setNewValue(newValueJson);
        auditLog.setUserName(changedBy);
        auditLog.setDeviceDetails(ipAddress);
        LocalDateTime timestamp = LocalDateTime.now();
        auditLog.setTimestamp(timestamp);
        auditLog.setSignature(DigitalSignatureUtil.signLog(action, changedBy, timestamp.toString()));

        auditLogProducer.sendAuditLog(auditLog);

        logger.info("AUDIT LOG SENT TO KAFKA: " + auditLog);

        return newEntity;
    }

    private Optional<?> getExistingEntity(Object entity) {
        try {
            Long id = (Long) entity.getClass().getMethod("getId").invoke(entity);
            return auditLogRepository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String convertToJson(Object obj) {
        try {
            return obj == null ? "null" : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON";
        }
    }
}
