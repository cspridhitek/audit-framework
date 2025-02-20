package com.ridhitek.audit.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;  // Ensure versioning

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // Action Taken

    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String signature;

    @Column(nullable = false)
    private String deviceDetails;


}