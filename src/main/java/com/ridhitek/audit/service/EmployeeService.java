package com.ridhitek.audit.service;

import org.springframework.stereotype.Service;

import com.ridhitek.audit.entity.Employee;
import com.ridhitek.audit.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository){
        this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Employee updatedEmployee) {
        return employeeRepository.save(updatedEmployee);
    }


    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
