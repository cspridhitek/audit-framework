package com.ridhitek.audit.repository;

import com.ridhitek.audit.entity.FailedAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedAuditLogRepository extends JpaRepository<FailedAuditLog, Long> {
}