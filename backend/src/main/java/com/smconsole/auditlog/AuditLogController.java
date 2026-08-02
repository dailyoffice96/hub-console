package com.smconsole.auditlog;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditLogs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getLog(
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) AuditTargetType targetType,
            @RequestParam(required = false) AuditAction action,
            Pageable pageable
    ){
        Page<AuditLogResponse> auditLog = auditLogService.getLog(adminName, targetType, action, pageable);
        return ResponseEntity.ok(auditLog);
    }


}

