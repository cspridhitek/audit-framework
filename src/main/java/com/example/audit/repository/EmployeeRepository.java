package com.example.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.audit.entity.Employee;

public interface EmployeeRepository extends  JpaRepository<Employee, Long> {
}
