package com.example.audit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.audit.annotation.Auditable;
import com.example.audit.entity.Employee;
import com.example.audit.repository.EmployeeRepository;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository){
        this.employeeRepository = employeeRepository;
    }

    @Auditable()
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Auditable(action = "UPDATE")
    public Employee updateEmployee(Employee updatedEmployee) {
        return employeeRepository.save(updatedEmployee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Auditable
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
