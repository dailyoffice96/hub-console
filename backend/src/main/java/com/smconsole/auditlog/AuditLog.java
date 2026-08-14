package com.smconsole.auditlog;

import com.smconsole.admin.entity.Admin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_log_admin_id", columnList = "admin_id"),
        @Index(name = "idx_audit_log_action", columnList = "action"),
        @Index(name = "idx_audit_log_target_type", columnList = "target_type")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private AuditAction action;

    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private AuditTargetType targetType;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}

