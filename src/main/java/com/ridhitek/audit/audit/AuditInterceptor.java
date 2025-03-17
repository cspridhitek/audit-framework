package com.ridhitek.audit.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridhitek.audit.annotation.ExcludeAuditField;
import com.ridhitek.audit.config.AuditProperties;
import com.ridhitek.audit.entity.AuditLog;
import com.ridhitek.audit.entity.FailedAuditLog;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.util.DigitalSignatureUtil;
import jakarta.transaction.Transactional;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuditInterceptor extends EmptyInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuditInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AuditLogRepository auditLogRepository;
    private final AuditLogProducer auditLogProducer;
    private final AuditProperties auditProperties;

    public AuditInterceptor(ApplicationContext context) {
        this.auditLogRepository = context.getBean(AuditLogRepository.class);
        this.auditLogProducer = context.getBean(AuditLogProducer.class);
        this.auditProperties = context.getBean(AuditProperties.class);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
                                String[] propertyNames, Type[] types) {
        if (previousState != null) {
            logAudit(entity, id, currentState, previousState, propertyNames, "UPDATE");
        }
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        logAudit(entity, id, state, null, propertyNames, "CREATE");
        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        logAudit(entity, id, null, state, propertyNames, "DELETE");
        super.onDelete(entity, id, state, propertyNames, types);
    }

    private void logAudit(Object entity, Serializable id, Object[] newState, Object[] oldState, String[] propertyNames, String action) {
        try {
            if (shouldSkipAudit(entity)) {
                return;
            }

            Map<String, Object> oldValues = new HashMap<>();
            Map<String, Object> newValues = new HashMap<>();

            if (!extractChangedFields(entity, oldState, newState, propertyNames, oldValues, newValues)) {
                return; // No relevant changes, skip logging
            }

            AuditLog auditLog = buildAuditLog(action, oldValues, newValues);
            saveAuditLogAsync(auditLog);
        } catch (Exception e) {
            logger.error("Failed to log audit for action: " + action, e);
        }
    }

    private boolean shouldSkipAudit(Object entity) {
        return entity instanceof AuditLog || entity instanceof FailedAuditLog;
    }

    private boolean extractChangedFields(Object entity, Object[] oldState, Object[] newState,
                                         String[] propertyNames, Map<String, Object> oldValues,
                                         Map<String, Object> newValues) {
        boolean hasChanges = false;
        for (int i = 0; i < propertyNames.length; i++) {
            if (isFieldExcluded(entity, propertyNames[i])) {
                continue;
            }

            Object oldValue = (oldState != null) ? oldState[i] : null;
            Object newValue = (newState != null) ? newState[i] : null;

            if (!Objects.equals(oldValue, newValue)) {
                hasChanges = true;
                oldValues.put(propertyNames[i], oldValue);
                newValues.put(propertyNames[i], newValue);
            }
        }
        return hasChanges;
    }

    private AuditLog buildAuditLog(String action, Map<String, Object> oldValues, Map<String, Object> newValues) {
        AuditLog auditLog = new AuditLog();
        String changedBy = "SYSTEM";
        auditLog.setUserName(changedBy);
        auditLog.setOldValue(convertToJson(oldValues));
        auditLog.setNewValue(convertToJson(newValues));
        auditLog.setAction(action);
        LocalDateTime timestamp = LocalDateTime.now();
        auditLog.setTimestamp(timestamp);
        auditLog.setSignature(DigitalSignatureUtil.signLog(action + changedBy + timestamp));
        auditLog.setDeviceDetails(getClientIpAddress());

        return auditLog;
    }

    @Transactional
    public void saveAuditLogAsync(AuditLog auditLog) {
        executorService.submit(() -> saveAuditLog(auditLog));
    }

    void saveAuditLog(AuditLog auditLog) {
        String handlerType = auditProperties.getHandlerType();
        try {
            if ("kafka_database".equals(handlerType)) {
                auditLogProducer.logToKafka(auditLog);
            } else {
                auditLogRepository.save(auditLog);
            }
        } catch (Exception e) {
            logger.error("Failed to save audit log", e);
        }
    }

    private String convertToJson(Map<String, Object> map) {
        try {
            boolean allNull = map.values().stream().allMatch(Objects::isNull);
            return allNull ? "{}" : objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr() != null ? request.getRemoteAddr() : "UNKNOWN";
        }
        return "UNKNOWN";
    }

    private boolean isFieldExcluded(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            return field.isAnnotationPresent(ExcludeAuditField.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
