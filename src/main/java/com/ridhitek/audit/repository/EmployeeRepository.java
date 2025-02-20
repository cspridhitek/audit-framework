package com.ridhitek.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ridhitek.audit.entity.Employee;

public interface EmployeeRepository extends  JpaRepository<Employee, Long> {
}
