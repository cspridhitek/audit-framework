package com.example.audit.aspect;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.audit.annotation.Auditable;
import com.example.audit.entity.AuditLog;
import com.example.audit.entity.Employee;
import com.example.audit.repository.AuditLogRepository;
import com.example.audit.repository.EmployeeRepository;
import com.example.audit.service.AuditService;
import com.example.audit.util.DigitalSignatureUtil;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LogManager.getLogger(AuditAspect.class);
    private Employee oldEmployee;

    @Autowired
    private AuditService auditService;

     @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    
    @Before("@annotation(auditable) && args(employee,..)")
    public void beforeLogAudit(JoinPoint joinPoint, Auditable auditable, Employee employee) {
        Optional<Employee> existingEmployee = employeeRepository.findById(employee.getId());
        existingEmployee.ifPresent(emp -> oldEmployee = cloneEmployee(emp));
    }
      // Utility to clone Employee to avoid reference issues
      private Employee cloneEmployee(Employee emp) {
        Employee cloned = new Employee();
        cloned.setId(emp.getId());
        cloned.setName(emp.getName());
        cloned.setDepartment(emp.getDepartment());
        cloned.setSalary(emp.getSalary());
        return cloned;
    }

    // Capture new value and differentiate by method name or annotation metadata
    @After("@annotation(auditable) && args(employee,..)")
    public void captureNewValue(JoinPoint joinPoint, Auditable auditable, Employee employee) {
            String action = joinPoint.getSignature().getName();
            
            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setOldValue("Name: " + oldEmployee.getName() +
            ", Dept: " + oldEmployee.getDepartment() +
            ", Salary: " + oldEmployee.getSalary());

            log.setNewValue("Name: " + employee.getName() +
            ", Dept: " + employee.getDepartment() +
            ", Salary: " + employee.getSalary());
            LocalDateTime timeStamp = LocalDateTime.now();
            log.setTimestamp(timeStamp);
            log.setChangedBy("nirmala");
            log.setSignature(DigitalSignatureUtil.signLog(action, "nirmala", timeStamp.toString()));
            auditLogRepository.save(log);
    }

}