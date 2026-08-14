package com.smconsole.admin.dto;

import com.smconsole.admin.enums.AdminRole;

public record AdminCreateRequest (
    String loginId,
    String password,
    String name,
    AdminRole role
){ }
