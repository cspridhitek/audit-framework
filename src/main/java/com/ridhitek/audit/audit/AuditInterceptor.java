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

import jakarta.servlet.http.HttpServletRequest;

public class AuditInterceptor extends EmptyInterceptor {

    private final ApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditInterceptor(ApplicationContext context) {
        this.context = context;
    }

    private AuditLogRepository getAuditLogRepository() {
        return context.getBean(AuditLogRepository.class); // Lazily fetch repository
    }
    private AuditLogProducer getAuditLogProducer(){
        return context.getBean(AuditLogProducer.class);
    }
    private AuditProperties getAuditProperties(){
        return context.getBean(AuditProperties.class);
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

        if (entity instanceof AuditLog) { // ✅ Avoid recursive logging
            return;
        }
        if(entity instanceof FailedAuditLog){
            return;
        }
        boolean hasRelevantChanges = false;

        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();

        for (int i = 0; i < propertyNames.length; i++) {

            if (isFieldExcluded(entity, propertyNames[i])) {
                continue;
            }
            Object oldValue = (oldState != null) ? oldState[i] : null;
            Object newValue = (newState != null) ? newState[i] : null;

            if (!Objects.equals(oldValue, newValue)) {
                hasRelevantChanges = true;
                oldValues.put(propertyNames[i], oldValue);
                newValues.put(propertyNames[i], newValue);
            }
        }
        if(!hasRelevantChanges){
            return;
        }
        String oldJson = convertToJson(oldValues);
        String newJson = convertToJson(newValues);
        LocalDateTime timestamp = LocalDateTime.now();
//        String ipAddress = getClientIpAddress(); // ✅ Fetch correct IP

        AuditLog auditLog = new AuditLog();
        String changedBy = "SYSTEM"; // Default Name
        auditLog.setUserName(changedBy);
        auditLog.setOldValue(oldJson);
        auditLog.setNewValue(newJson);
        auditLog.setAction(action);
        auditLog.setTimestamp(timestamp);
        auditLog.setSignature(DigitalSignatureUtil.signLog(action + changedBy + timestamp));
        auditLog.setDeviceDetails("ipAddress"); // ✅ Store real IP

        saveAuditLog(auditLog); // ✅ Use separate transaction to avoid conflicts
    }

    @Transactional
    public void saveAuditLog(AuditLog auditLog) {
        AuditProperties auditProperties = getAuditProperties();
        String handlerType = auditProperties.getHandlerType();
        System.out.println("Save Audit log"+ handlerType);
        if(Objects.equals(handlerType, "kafka_database")){
            AuditLogProducer auditLogProducer = getAuditLogProducer();
            auditLogProducer.logToKafka(auditLog);
            return;
        }
        AuditLogRepository auditLogRepository = getAuditLogRepository();
        System.out.println("Saving to Database   " +auditLog);
        auditLogRepository.save(auditLog);
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
