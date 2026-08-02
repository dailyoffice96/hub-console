package com.smconsole.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.admin ORDER BY a.createdAt DESC")
    Page<AuditLog> findAllFetch(Pageable pageable);

    Page<AuditLog> findByAdmin_NameContaining(String adminName, Pageable pageable);
    Page<AuditLog> findByTargetType(AuditTargetType targetType, Pageable pageable);
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);
}

