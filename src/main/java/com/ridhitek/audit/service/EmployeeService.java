package com.ridhitek.audit.service;

import org.springframework.stereotype.Service;

import com.ridhitek.audit.annotation.Auditable;
import com.ridhitek.audit.entity.Employee;
import com.ridhitek.audit.repository.EmployeeRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository){
        this.employeeRepository = employeeRepository;
    }

    @Auditable(action = "CREATE EMPLOYEE")
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Auditable(action = "UPDATE EMPLOYEE")
    public Employee updateEmployee(Employee updatedEmployee) {
        return employeeRepository.save(updatedEmployee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Employee getEmployeeById(Long id){
        return  employeeRepository.findById(id).orElse(null);
    }

    @Auditable
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
