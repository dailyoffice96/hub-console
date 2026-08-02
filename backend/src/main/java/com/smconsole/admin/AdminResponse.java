package com.smconsole.admin;

import java.time.LocalDate;

public record AdminResponse (
    Long id,
    String loginId,
    String name,
    AdminRole role,
    boolean isLocked,
    LocalDate createdAt
){ }
