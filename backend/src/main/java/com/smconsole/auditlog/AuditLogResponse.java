package com.smconsole.auditlog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String admin,
        AuditAction action,
        Long targetId,
        AuditTargetType targetType,
        String detail,
        LocalDateTime createdAt
) { }
