package com.ridhitek.audit.entity;



import com.ridhitek.audit.annotation.ExcludeAuditField;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ExcludeAuditField
    private String department;

    private Double salary;
}
