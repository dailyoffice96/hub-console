package com.smconsole.admin.dto;

import com.smconsole.admin.enums.AdminRole;

import java.time.LocalDate;

public record AdminResponse (
    Long id,
    String loginId,
    String name,
    AdminRole role,
    boolean isLocked,
    LocalDate createdAt
){ }
