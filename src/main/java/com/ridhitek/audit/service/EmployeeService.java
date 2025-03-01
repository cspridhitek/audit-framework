package com.ridhitek.audit.service;

import org.springframework.stereotype.Service;

import com.ridhitek.audit.annotation.Auditable;
import com.ridhitek.audit.entity.Employee;
import com.ridhitek.audit.repository.EmployeeRepository;

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


    @Auditable(action = "DELETE EMPLOYEE")
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
