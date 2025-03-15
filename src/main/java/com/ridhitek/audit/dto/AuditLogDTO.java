package com.ridhitek.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String userName;
    private String action;
    private String oldValue;
    private String deviceDetails;
    private LocalDateTime timestamp;
    private String newValue;
}
