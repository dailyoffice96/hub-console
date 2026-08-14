package com.smconsole.admin.entity;

import com.smconsole.admin.enums.AdminRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admins", indexes = {
        @Index(name = "idx_admin_name", columnList = "name"),
        @Index(name = "idx_admin_role", columnList = "role"),
})
public class Admin{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="admin_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private AdminRole role;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name="login_fail_count", nullable = false)
    private int loginFailCount = 0;

    @Column(name="is_locked")
    private boolean isLocked = false;

    @Column(name="created_at")
    private LocalDate createdAt = LocalDate.now();

}
