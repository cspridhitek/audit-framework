package com.ridhitek.audit.controller;

import com.ridhitek.audit.entity.Employee;
import com.ridhitek.audit.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock  // Mocking EmployeeService
    private EmployeeService employeeService;

    @InjectMocks  // Injecting the mock into EmployeeController
    private EmployeeController employeeController;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setName("John Doe");
        mockEmployee.setDepartment("IT");
    }

    @Test
    void testCreateEmployee() {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(mockEmployee);

        Employee createdEmployee = employeeController.createEmployee(mockEmployee);

        assertNotNull(createdEmployee);
        assertEquals(1L, createdEmployee.getId());
        assertEquals("John Doe", createdEmployee.getName());

        verify(employeeService, times(1)).createEmployee(any(Employee.class)); // Verify interaction
    }

    @Test
    void testUpdateEmployee() {
        when(employeeService.updateEmployee(any(Employee.class))).thenReturn(mockEmployee);

        Employee updatedEmployee = employeeController.updateEmployee(mockEmployee);

        assertNotNull(updatedEmployee);
        assertEquals(1L, updatedEmployee.getId());
        assertEquals("John Doe", updatedEmployee.getName());

        verify(employeeService, times(1)).updateEmployee(any(Employee.class));
    }

    @Test
    void testDeleteEmployee() {
        doNothing().when(employeeService).deleteEmployee(1L);

        employeeController.deleteEmployee(1L);

        verify(employeeService, times(1)).deleteEmployee(1L); // Verify delete method is called
    }
}
