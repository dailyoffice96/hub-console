package com.smconsole.auditlog;

import com.smconsole.admin.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Admin admin, AuditAction action, AuditTargetType targetType, Long targetId, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAdmin(admin);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetail(detail);
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getLog(
            String adminName, AuditTargetType targetType, AuditAction action, Pageable pageable
    ) {
        Page<AuditLog> auditLog;

        if (adminName != null && !adminName.isEmpty()) {
            auditLog = auditLogRepository.findByAdmin_NameContaining(adminName, pageable);
        } else if (targetType != null) {
            auditLog = auditLogRepository.findByTargetType(targetType, pageable);
        } else if (action != null) {
            auditLog = auditLogRepository.findByAction(action, pageable);
        } else {
            auditLog = auditLogRepository.findAllFetch(pageable);
        }

        return auditLog.map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAdmin() != null ? auditLog.getAdmin().getName() : null,
                auditLog.getAction(),
                auditLog.getTargetId(),
                auditLog.getTargetType(),
                auditLog.getDetail(),
                auditLog.getCreatedAt()
        );
    }
}